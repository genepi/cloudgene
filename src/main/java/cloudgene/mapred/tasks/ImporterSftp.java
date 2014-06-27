package cloudgene.mapred.tasks;

import genepi.hadoop.HdfsUtil;

import java.io.IOException;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

public class ImporterSftp extends AbstractTask {
	

	private String server;

	private String workingDir;

	private String username;

	private String password;

	private String path;

	private String url;
	
	private int port;

	private long size = 0;

	private long read = 0;
	
	private CountingOutputStream t;

	public ImporterSftp(String server, String username, String password,
			String path, int port) {

		setName("import-sftp");
		
		this.username = username.trim();
		this.password = password;
		this.path = path;
		this.url = server;
		this.port = port;
		
		System.out.println(username);
		System.out.println(password);
		System.out.println(server);
		System.out.println(path);

		String server1 = server.replace("sftp://", "");
		String split[] = server1.split("/", 2);
		this.server = split[0].trim();
		workingDir = "/" + split[1].trim();

		//size = getSize(this.server, workingDir, this.username, this.password, this.port);

	}

	@Override
	public boolean execute() {

		Configuration conf = new Configuration();
		FileSystem fileSystem;
		try {
			fileSystem = FileSystem.get(conf);
			return importIntoHdfs(server, workingDir, username, password,
					fileSystem, path, port);
		} catch (IOException e) {
		e.printStackTrace();
			return false;
		} catch (JSchException e) {
			e.printStackTrace();
			return false;
		} catch (SftpException e) {
			e.printStackTrace();
			return false;
		}

	}

	@SuppressWarnings("unchecked")
	private long getSize(String server, String workingDir, String username,
			String password, int port)  {

		long size = 0;

		
		Session 	session 	= null;
		Channel 	channel 	= null;
		ChannelSftp channelSftp = null;
		
		JSch jsch = new JSch();
		
		
		try {
			session = jsch.getSession(username,server,port);
			session.setPassword(password);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
			channel = session.openChannel("sftp");
			channel.connect();
			channelSftp = (ChannelSftp)channel;
			
			Vector<ChannelSftp.LsEntry> filelist = null;
		
			filelist = channelSftp.ls(workingDir);

			for (ChannelSftp.LsEntry entry : filelist) {

				// Check if FTPFile is a regular file
				if (!entry.getAttrs().isDir() && !((entry.getFilename().equals(".") || (entry
						.getFilename().equals(".."))))) {
					if (entry.getAttrs().isLink()) {
						SftpATTRS linkattr = channelSftp.lstat(workingDir);
											
						if (!linkattr.isDir()){
							size +=linkattr.getSize(); 
						}
					}
					
					size += entry.getAttrs().getSize();
				}
			}

			channel.disconnect();
			session.disconnect();

		} catch (Exception e) {

			return 0;

		} finally {

			channel.disconnect();
			session.disconnect();
		}

		return size;

	}

	@SuppressWarnings("unchecked")
	public boolean importIntoHdfs(String server, String workingDir,
			String username, String password, FileSystem fileSystem, String path, int port)
			throws IOException, JSchException, SftpException {


		Session 	session 	= null;
		Channel 	channel 	= null;
		ChannelSftp channelSftp = null;
		
		JSch jsch = new JSch();
		
	
		try {
			
			session = jsch.getSession(username,server,port);
			session.setPassword(password);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
			channel = session.openChannel("sftp");
			channel.connect();
			channelSftp = (ChannelSftp)channel;
			
			Vector<ChannelSftp.LsEntry> filelist = null;
			
			if ( channelSftp.lstat(workingDir).isDir() ) {

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
				if (!linkIsdir && !entry.getAttrs().isDir() && !((entry.getFilename().equals(".") || (entry
						.getFilename().equals(".."))))) {

					// path in hdfs
					String[] tiles = entry.getFilename().split("/");
					String name = tiles[tiles.length - 1];

					String target = HdfsUtil.path(path, name);


					FSDataOutputStream out = fileSystem
							.create(new Path(target));

					t = new CountingOutputStream(out);
					
					channelSftp.get(workingDir + "/" + entry.getFilename(), t);

					IOUtils.closeStream(out);

					read += entry.getAttrs().getSize();

				}
			}
			channelSftp.disconnect();
		}catch(Exception e){
			e.printStackTrace();
			return false;
		} finally {
			session.disconnect();
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
		return new String[] { "Folder-Name", "Size", "Type", "SFTP-Address" };
	}

	@Override
	public String[] getValues() {
		return new String[] { path, FileUtils.byteCountToDisplaySize(size),
				"SFTP-Server", url };
	}

}
