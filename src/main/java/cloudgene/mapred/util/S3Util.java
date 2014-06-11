package cloudgene.mapred.util;

import java.io.File;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Object;
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

public class S3Util {

	public static boolean checkBucket(String awsKey, String awsSecretKey,
			String bucket) {

		AWSCredentials awsCredentials = new AWSCredentials(awsKey, awsSecretKey);
		try {

			ThreadedStorageService s3Service = new ThreadedStorageService(
					new RestS3Service(awsCredentials),
					new StorageServiceEventListener() {

						@Override
						public void event(DownloadObjectsEvent event) {

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

			s3Service.getStorageService().listObjects(bucket, "", "");
			return true;

		} catch (Exception e) {

			return false;

		}
	}

	public static void copyHdfsDirectory(String awsKey, String awsSecretKey,
			String bucket, String directory, String hdfs) {

		AWSCredentials awsCredentials = new AWSCredentials(awsKey, awsSecretKey);

		try {

			ThreadedStorageService s3Service = new ThreadedStorageService(
					new RestS3Service(awsCredentials),
					new StorageServiceEventListener() {

						@Override
						public void event(DownloadObjectsEvent event) {

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

			Configuration conf = new Configuration();

			FileSystem fileSystem = FileSystem.get(conf);
			Path pathFolder = new Path(hdfs);
			FileStatus[] files = fileSystem.listStatus(pathFolder);

			if (files != null) {
				for (FileStatus file : files) {
					Path path = file.getPath();
					if (!file.isDir()
							&& !file.getPath().getName().startsWith("_")) {
						FSDataInputStream in = fileSystem.open(path);

						S3Object object = new S3Object(directory + "/"
								+ pathFolder.getName() + "/" + path.getName());
						object.setDataInputStream(in);
						object.setContentLength(file.getLen());
						object.setContentType("text/plain");

						s3Service.getStorageService().putObject(bucket, object);

						in.close();
					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void copyHdfsFile(String awsKey, String awsSecretKey,
			String bucket, String directory, String hdfs) {

		AWSCredentials awsCredentials = new AWSCredentials(awsKey, awsSecretKey);

		try {

			ThreadedStorageService s3Service = new ThreadedStorageService(
					new RestS3Service(awsCredentials),
					new StorageServiceEventListener() {

						@Override
						public void event(DownloadObjectsEvent event) {

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

			Configuration conf = new Configuration();

			FileSystem fileSystem = FileSystem.get(conf);
			Path path = new Path(hdfs);

			FSDataInputStream in = fileSystem.open(path);

			FileStatus file = fileSystem.getFileStatus(path);

			S3Object object = new S3Object(directory + "/" + path.getName());
			object.setDataInputStream(in);
			object.setContentLength(file.getLen());
			object.setContentType("text/plain");

			s3Service.getStorageService().putObject(bucket, object);

			in.close();

		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	public static void copyDirectory(String awsKey, String awsSecretKey,
			String bucket, String directory, String folder) {

		AWSCredentials awsCredentials = new AWSCredentials(awsKey, awsSecretKey);

		try {

			ThreadedStorageService s3Service = new ThreadedStorageService(
					new RestS3Service(awsCredentials),
					new StorageServiceEventListener() {

						@Override
						public void event(DownloadObjectsEvent event) {

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

			File folderFile = new File(folder);

			File[] files = folderFile.listFiles();

			for (File file : files) {

				if (!file.isDirectory()) {

					S3Object object = new S3Object(file);
					object.setKey(directory + "/" + folderFile.getName() + "/"
							+ file.getName());
					s3Service.getStorageService().putObject(bucket, object);

				}

			}

		} catch (Exception e) {

			e.printStackTrace();

		}
	}

	public static void copyFile(String awsKey, String awsSecretKey,
			String bucket, String directory, String filename) {

		AWSCredentials awsCredentials = new AWSCredentials(awsKey, awsSecretKey);

		try {

			ThreadedStorageService s3Service = new ThreadedStorageService(
					new RestS3Service(awsCredentials),
					new StorageServiceEventListener() {

						@Override
						public void event(DownloadObjectsEvent event) {

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

			File file = new File(filename);

			S3Object object = new S3Object(file);
			object.setKey(directory + "/" + file.getName());
			s3Service.getStorageService().putObject(bucket, object);

		} catch (Exception e) {

			e.printStackTrace();

		}

	}

}
