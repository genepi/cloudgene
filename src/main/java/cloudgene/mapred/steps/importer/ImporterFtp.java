package cloudgene.mapred.steps.importer;

import genepi.hadoop.HdfsUtil;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

import cloudgene.mapred.util.FileItem;

public class ImporterFtp implements IImporter {

	private String server;

	private String workingDir;

	private String username = "anonymous";

	private String password = "anonymous@domain.com";

	private String path;

	private CountingOutputStream t;

	private String error;

	public ImporterFtp(String url, String path) {

		this.server = url.split(";")[0];
		if (url.split(";").length > 1) {
			this.username = url.split(";")[1].trim();
		}
		if (url.split(";").length > 2) {
			this.password = url.split(";")[2];
		}
		this.path = path;

		String server1 = server.replace("ftp://", "");
		String split[] = server1.split("/", 2);
		this.server = split[0].trim();
		workingDir = split[1].trim();
	}

	@Override
	public boolean importFiles() {
		return importFiles(null);				
	}
	
	@Override
	public boolean importFiles(String extension) {

		Configuration conf = new Configuration();
		FileSystem fileSystem;
		try {

			fileSystem = FileSystem.get(conf);

			return importIntoHdfs(server, workingDir, username, password,
					fileSystem, path);

		} catch (IOException e) {
			error = e.getMessage();
			return false;
		}

	}	

	public boolean importIntoHdfs(String server, String workingDir,
			String username, String password, FileSystem fileSystem, String path)
			throws IOException {

		if (username == null || username.equals("")) {
			username = "anonymous";
		}

		FTPClient client = new FTPClient();
		try {

			client.connect(server);
			client.enterLocalPassiveMode();
			if (client.login(username, password)) {

				FTPFile[] ftpFiles = null;

				if (client.changeWorkingDirectory(workingDir)) {

					// directory
					ftpFiles = client.listFiles();

				} else {

					// file

					String filename = workingDir;
					String[] tiles = workingDir.split("/");
					workingDir = "";
					for (int i = 0; i < tiles.length - 1; i++) {
						workingDir += tiles[i];
						if (i < tiles.length - 2) {
							workingDir += "/";
						}
					}

					ftpFiles = client.listFiles(filename);

					if (ftpFiles.length == 0) {
						error = "file not found";
						return false;
					}

					client.changeWorkingDirectory(workingDir);
				}

				for (FTPFile ftpFile : ftpFiles) {

					// Check if FTPFile is a regular file
					if (ftpFile.getType() == FTPFile.FILE_TYPE) {

						// path in hdfs
						String[] tiles = ftpFile.getName().split("/");
						String name = tiles[tiles.length - 1];

						String target = HdfsUtil.path(path, name);

						FSDataOutputStream out = fileSystem.create(new Path(
								target));

						t = new CountingOutputStream(out);

						client.retrieveFile(ftpFile.getName(), t);

						IOUtils.closeStream(out);

					}
				}
				client.logout();

			} else {
				error = "access denied";
				return false;

			}
		} catch (Exception e) {
			error = e.getMessage();
			return false;

		} finally {
			client.disconnect();
		}
		return true;
	}

	@Override
	public List<FileItem> getFiles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getErrorMessage() {
		return error;
	}

}
