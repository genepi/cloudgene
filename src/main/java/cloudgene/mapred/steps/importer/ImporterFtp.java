package cloudgene.mapred.steps.importer;

import genepi.hadoop.HdfsUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

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
			e.printStackTrace();
			error = e.getMessage();
			return false;
		}

	}

	public boolean importIntoHdfs(String server, String workingDir,
			String username, String password, FileSystem fileSystem, String path)
			throws IOException {

		boolean commandOK = true;

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

						client.setFileType(FTP.BINARY_FILE_TYPE);
						client.enterLocalPassiveMode();
						client.setAutodetectUTF8(true);

						InputStream inputStream = client
								.retrieveFileStream(name);

						IOUtils.copy(inputStream, out);
						out.flush();

						IOUtils.closeQuietly(out);
						IOUtils.closeQuietly(inputStream);
						commandOK = client.completePendingCommand();
					}
				}
				client.logout();

			} else {
				error = "access denied";
				return false;

			}
		} catch (Exception e) {
			e.printStackTrace();
			error = e.getMessage();
			return false;

		} finally {
			client.disconnect();
		}
		return commandOK;
	}

	@Override
	public List<FileItem> getFiles() {
		List<FileItem> items = new Vector<FileItem>();
		FileItem file = new FileItem();
		file.setText(FilenameUtils.getName(workingDir));
		file.setPath("/");
		file.setId("/");
		file.setSize("-");
		items.add(file);
		return items;
	}

	@Override
	public String getErrorMessage() {
		return error;
	}

}
