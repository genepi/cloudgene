package cloudgene.mapred.jobs.workspace;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import cloudgene.mapred.jobs.Download;

public interface IWorkspace {

	public void setup(String job) throws IOException;

	public String upload(String id, File file) throws IOException;
	
	public String uploadInput(String id, File file) throws IOException;

	public InputStream download(String url) throws IOException;

	public void delete(String job) throws IOException;

	public String getName();

	public String createPublicLink(String url);
	
	public String getParent(String url);
	
	public String createFolder(String id);

	public String createFile(String name, String name2);

	public String createLogFile(String name);

	public String createTempFolder(String string);

	public List<Download> getDownloads(String url);
	
	public void cleanup(String job) throws IOException;
	
	public boolean exists(String path) throws IOException;
	
}
