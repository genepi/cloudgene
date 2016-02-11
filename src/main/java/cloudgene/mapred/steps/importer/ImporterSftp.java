package cloudgene.mapred.steps.importer;

import genepi.hadoop.HdfsUtil;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

import cloudgene.mapred.util.FileItem;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class ImporterSftp implements IImporter {

	private String server;

	private String workingDir;

	private String username = "anonymous";

	private String password = "anonymous@domain.com";

	private String path;

	private int port;

	private CountingOutputStream t;

	private String error;

	public ImporterSftp(String url, String path) {

		this.server = url.split(";")[0];
		if (url.split(";").length > 1) {
			this.username = url.split(";")[1].trim();
		}
		if (url.split(";").length > 2) {
			this.password = url.split(";")[2];
		}
		this.path = path;
		this.port = 22;

		String server1 = server.replace("sftp://", "");
		String split[] = server1.split("/", 2);
		this.server = split[0].trim();

		if (split.length > 1) {
			workingDir = "/" + split[1].trim();
		} else {
			workingDir = "/";
		}

	}

	@Override
	public boolean importFiles() {
		return importFiles(null);
	}

	@Override
	public boolean importFiles(String extension) {

		try {
			FileSystem fileSystem = HdfsUtil.getFileSystem();
			return importIntoHdfs(server, workingDir, username, password,
					fileSystem, path, port, extension);
		} catch (Exception e) {
			error = e.getMessage();
			e.printStackTrace();
			return false;
		}

	}

	@SuppressWarnings("unchecked")
	private boolean importIntoHdfs(String server, String workingDir,
			String username, String password, FileSystem fileSystem,
			String path, int port, String extension) throws IOException,
			JSchException, SftpException {

		Session session = null;
		Channel channel = null;
		ChannelSftp channelSftp = null;

		JSch jsch = new JSch();

		try {

			session = jsch.getSession(username, server, port);
			session.setPassword(password);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
			channel = session.openChannel("sftp");
			channel.connect();
			channelSftp = (ChannelSftp) channel;

			Vector<ChannelSftp.LsEntry> filelist = null;

			if (channelSftp.lstat(workingDir).isDir()) {

				// directory
				filelist = channelSftp.ls(workingDir);

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

				channelSftp.cd(workingDir);
				filelist = channelSftp.ls(filename);

			}

			for (ChannelSftp.LsEntry entry : filelist) {
				boolean linkIsdir = false;
				if (entry.getAttrs().isLink()) {
					String link = null;
					link = channelSftp.readlink(entry.getFilename());
					linkIsdir = channelSftp.lstat(link).isDir();
				}

				// Check if FTPFile is a regular file
				if (!linkIsdir
						&& !entry.getAttrs().isDir()
						&& !((entry.getFilename().equals(".") || (entry
								.getFilename().equals(".."))))) {

					boolean needImport = false;
					if (extension == null) {
						needImport = true;
					}

					if (!needImport) {
						String[] exts = extension.split("|");
						for (String ext : exts) {
							if (!needImport) {
								if (entry.getFilename().endsWith(ext)) {
									needImport = true;
								}
							}
						}
					}

					if (needImport) {

						// path in hdfs
						String[] tiles = entry.getFilename().split("/");
						String name = tiles[tiles.length - 1];

						String target = HdfsUtil.path(path, name);

						FSDataOutputStream out = fileSystem.create(new Path(
								target));

						t = new CountingOutputStream(out);

						channelSftp.get(workingDir + "/" + entry.getFilename(),
								t);

						IOUtils.closeStream(out);

					}

				}
			}
			channelSftp.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
			error = e.getMessage();
			return false;
		} finally {
			session.disconnect();
		}
		return true;
	}

	@Override
	public List<FileItem> getFiles() {

		List<FileItem> results = new Vector<FileItem>();

		Session session = null;
		Channel channel = null;
		ChannelSftp channelSftp = null;

		JSch jsch = new JSch();

		try {

			session = jsch.getSession(username, server, port);
			session.setPassword(password);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
			channel = session.openChannel("sftp");
			channel.connect();
			channelSftp = (ChannelSftp) channel;

			Vector<ChannelSftp.LsEntry> filelist = null;

			if (channelSftp.lstat(workingDir).isDir()) {

				// directory
				filelist = channelSftp.ls(workingDir);

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

				channelSftp.cd(workingDir);
				filelist = channelSftp.ls(filename);

			}

			for (ChannelSftp.LsEntry entry : filelist) {
				boolean linkIsdir = false;
				if (entry.getAttrs().isLink()) {
					String link = null;
					link = channelSftp.readlink(entry.getFilename());
					linkIsdir = channelSftp.lstat(link).isDir();
				}

				// Check if FTPFile is a regular file
				if (!linkIsdir
						&& !entry.getAttrs().isDir()
						&& !((entry.getFilename().equals(".") || (entry
								.getFilename().equals(".."))))) {

					// path in hdfs
					String[] tiles = entry.getFilename().split("/");
					String name = tiles[tiles.length - 1];

					FileItem item = new FileItem();
					item.setText(entry.getFilename());
					item.setPath("/");
					item.setId("/");
					item.setSize(FileUtils.byteCountToDisplaySize(entry
							.getAttrs().getSize()));

					results.add(item);

				}
			}
			channelSftp.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
			error = e.getMessage();
			return null;
		} finally {
			session.disconnect();
		}
		return results;

	}

	@Override
	public String getErrorMessage() {
		return error;
	}

}
