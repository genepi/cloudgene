package cloudgene.mapred.tasks;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Vector;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.StorageObject;
import org.jets3t.service.multi.DownloadPackage;
import org.jets3t.service.multi.StorageServiceEventListener;
import org.jets3t.service.multi.ThreadedStorageService;
import org.jets3t.service.multi.event.CopyObjectsEvent;
import org.jets3t.service.multi.event.CreateBucketsEvent;
import org.jets3t.service.multi.event.CreateObjectsEvent;
import org.jets3t.service.multi.event.DeleteObjectsEvent;
import org.jets3t.service.multi.event.DownloadObjectsEvent;
import org.jets3t.service.multi.event.GetObjectHeadsEvent;
import org.jets3t.service.multi.event.GetObjectsEvent;
import org.jets3t.service.multi.event.ListObjectsEvent;
import org.jets3t.service.multi.event.LookupACLEvent;
import org.jets3t.service.multi.event.UpdateACLEvent;
import org.jets3t.service.security.AWSCredentials;

import cloudgene.mapred.util.HdfsUtil;
import cloudgene.mapred.util.Settings;

public class ImporterS3 extends AbstractTask {

	private String bucket;

	private String key;

	private String secret;

	private String path;

	private float percentage = 0;

	public ImporterS3(String bucket, String key, String secret, String path) {

		setName("import-s3");
		this.bucket = bucket;
		this.key = key;
		this.secret = secret;
		this.path = path;

	}

	@Override
	public boolean execute() {

		Configuration conf = new Configuration();
		FileSystem fileSystem;
		try {
			fileSystem = FileSystem.get(conf);
			return importIntoHdfs(bucket, key, secret, fileSystem, path);
		} catch (S3ServiceException e) {
			writeOutput(e.getLocalizedMessage());
			return false;
		} catch (IOException e) {
			writeOutput(e.getLocalizedMessage());
			return false;
		} catch (ServiceException e) {
			writeOutput(e.getLocalizedMessage());
			return false;
		}

	}

	@Override
	public int getProgress() {

		return (int) (percentage * 100);

	}

	public boolean importIntoHdfs(String server, String key, String secret,
			FileSystem fileSystem, String path) throws IOException,
			ServiceException {

		AWSCredentials awsCredentials = null;

		if (key != null && !key.isEmpty() && secret != null
				&& !secret.isEmpty()) {

			awsCredentials = new AWSCredentials(key, secret);

		}

		try {

			ThreadedStorageService s3Service = new ThreadedStorageService(
					new RestS3Service(awsCredentials),
					new StorageServiceEventListener() {

						@Override
						public void event(DownloadObjectsEvent event) {
							// TODO Auto-generated method stub

							if (event.getEventCode() == DownloadObjectsEvent.EVENT_IN_PROGRESS
									|| event.getEventCode() == DownloadObjectsEvent.EVENT_STARTED) {

								percentage = event.getThreadWatcher()
										.getBytesTransferred()
										/ (float) event.getThreadWatcher()
												.getBytesTotal();
							}

							/*
							 * if (event.getThreadWatcher()){
							 * 
							 * }
							 */

						}

						@Override
						public void event(UpdateACLEvent arg0) {
							// TODO Auto-generated method stub

						}

						@Override
						public void event(LookupACLEvent arg0) {
							// TODO Auto-generated method stub

						}

						@Override
						public void event(GetObjectHeadsEvent arg0) {
							// TODO Auto-generated method stub

						}

						@Override
						public void event(GetObjectsEvent arg0) {
							// TODO Auto-generated method stub

						}

						@Override
						public void event(DeleteObjectsEvent arg0) {
							// TODO Auto-generated method stub

						}

						@Override
						public void event(CreateBucketsEvent arg0) {
							// TODO Auto-generated method stub

						}

						@Override
						public void event(CopyObjectsEvent arg0) {
							// TODO Auto-generated method stub

						}

						@Override
						public void event(CreateObjectsEvent arg0) {
							// TODO Auto-generated method stub

						}

						@Override
						public void event(ListObjectsEvent arg0) {
							// TODO Auto-generated method stub

						}
					});

			server = server.replace("s3n://", "");
			String tiles[] = server.split("/", 2);

			String bucketName = tiles[0];

			StorageObject[] list = null;

			String directory = "";

			boolean file = false;

			if (tiles.length > 1) {
				directory = tiles[1];

				StorageObject temp = null;

				try {
					temp = s3Service.getStorageService().getObject(bucketName,
							directory);

					if (temp.isDirectoryPlaceholder()) {

						if (!directory.endsWith("/")) {
							directory = directory + "/";
						}

						list = s3Service.getStorageService().listObjects(
								bucketName, directory, "");

					} else {

						list = new StorageObject[1];
						list[0] = temp;

						file = true;
					}

				} catch (ServiceException e) {

					if (!directory.endsWith("/")) {
						directory = directory + "/";
					}

					list = s3Service.getStorageService().listObjects(
							bucketName, directory, "");

				}

			} else {

				// whole bucket

				list = s3Service.getStorageService().listObjects(bucketName,
						null, "");

			}

			List<DownloadPackage> packages = new Vector<DownloadPackage>();
			for (StorageObject object : list) {

				// path in hdfs

				if (!object.isDirectoryPlaceholder()) {

					Settings settings = Settings.getInstance();
					String workspace = settings.getHdfsWorkspace(job.getUser()
							.getUsername());

					String target = "";

					if (!file) {

						target = HdfsUtil.path(workspace, path, object.getKey()
								.replaceAll(directory, ""));

						writeOutput("Downloading File " + object.getKey()
								+ "...");

					} else {

						String[] tiles2 = object.getKey().split("/");

						target = HdfsUtil.path(workspace, path,
								tiles2[tiles2.length - 1]);

						writeOutput("Downloading File " + object.getKey()
								+ "...");

					}

					OutputStream out = fileSystem.create(new Path(target));

					DownloadPackage p = new DownloadPackage(object, out);
					packages.add(p);
				}
			}

			DownloadPackage[] pack = new DownloadPackage[packages.size()];
			for (int i = 0; i < pack.length; i++) {
				pack[i] = packages.get(i);
			}

			s3Service.downloadObjects(bucketName, pack);

			s3Service.shutdown();

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public String[] getParameters() {
		return new String[] { "S3-Path", "Folder-Name", "Type", "Size" };
	}

	@Override
	public String[] getValues() {
		return new String[] { bucket, path, "S3", "? B" };
	}
}
