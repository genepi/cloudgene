package cloudgene.mapred.tasks;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

import cloudgene.mapred.util.HdfsUtil;
import cloudgene.mapred.util.Settings;

public class ImporterFtp extends AbstractTask {

	private String server;

	private String workingDir;

	private String username;

	private String password;

	private String path;

	private String url;

	private long size = 0;

	private long read = 0;

	private CountingOutputStream t;

	public ImporterFtp(String server, String username, String password,
			String path) {

		setName("import-ftp");
		
		this.username = username.trim();
		this.password = password;
		this.path = path;
		this.url = server;

		String server1 = server.replace("ftp://", "");
		String split[] = server1.split("/", 2);
		this.server = split[0].trim();
		workingDir = split[1].trim();

		size = getSize(this.server, workingDir, this.username, this.password);

	}

	@Override
	public boolean execute() {

		Configuration conf = new Configuration();
		FileSystem fileSystem;
		try {
			fileSystem = FileSystem.get(conf);
			return importIntoHdfs(server, workingDir, username, password,
					fileSystem, path);
		} catch (IOException e) {
			writeOutput(e.getLocalizedMessage());
			return false;
		}

	}

	private long getSize(String server, String workingDir, String username,
			String password) {

		long size = 0;

		if (username == null || username.equals("")) {
			username = "anonymous";
		}

		FTPClient client = new FTPClient();
		try {

			client.connect(server);
			client.enterLocalPassiveMode();
			client.login(username, password);
			FTPFile[] ftpFiles = null;
			if (client.changeWorkingDirectory(workingDir)) {
				// directory
				ftpFiles = client.listFiles();
			} else {
				// file
				ftpFiles = client.listFiles(workingDir);
			}

			for (FTPFile ftpFile : ftpFiles) {

				// Check if FTPFile is a regular file
				if (ftpFile.getType() == FTPFile.FILE_TYPE) {
					size += ftpFile.getSize();
				}
			}

			client.logout();

		} catch (Exception e) {

			return 0;

		} finally {

			try {
				client.disconnect();
			} catch (IOException e) {
				e.printStackTrace();

				return 0;
			}
		}

		return size;

	}

	public boolean importIntoHdfs(String server, String workingDir,
			String username, String password, FileSystem fileSystem, String path)
			throws IOException {

		if (username == null || username.equals("")) {
			username = "anonymous";
		}

		writeOutput("Server: '" + server + "...");

		FTPClient client = new FTPClient();
		try {

			client.connect(server);
			client.enterLocalPassiveMode();
			client.login(username, password);

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
				client.changeWorkingDirectory(workingDir);
			}

			for (FTPFile ftpFile : ftpFiles) {

				// Check if FTPFile is a regular file
				if (ftpFile.getType() == FTPFile.FILE_TYPE) {

					// path in hdfs
					String[] tiles = ftpFile.getName().split("/");
					String name = tiles[tiles.length - 1];

					Settings settings = Settings.getInstance();
					String workspace = settings.getHdfsWorkspace(job.getUser()
							.getUsername());

					String target = HdfsUtil.path(workspace, path, name);

					writeOutput("Downloading file " + ftpFile.getName() + "...");

					FSDataOutputStream out = fileSystem
							.create(new Path(target));

					t = new CountingOutputStream(out);

					client.retrieveFile(ftpFile.getName(), t);

					IOUtils.closeStream(out);

					read += ftpFile.getSize();

				}
			}
			client.logout();
		} finally {
			client.disconnect();
		}
		return true;
	}

	@Override
	public int getProgress() {

		if (t != null) {

			return (int) ((read + t.getByteCount()) / (double) size * 100d);

		} else {
			return 0;
		}

	}

	@Override
	public String[] getParameters() {
		return new String[] { "Folder-Name", "Size", "Type", "FTP-Address" };
	}

	@Override
	public String[] getValues() {
		return new String[] { path, FileUtils.byteCountToDisplaySize(size),
				"FTP-Server", url };
	}

}
