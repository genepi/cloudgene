package cloudgene.mapred.jobs.workspace;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import cloudgene.mapred.jobs.AbstractJob;

public interface IExternalWorkspace {

	public void setup(AbstractJob job)throws IOException;
	
	public String upload(String id, File file)throws IOException;
	
	public InputStream download(String url)throws IOException;
	
	public void delete(AbstractJob job)throws IOException;
	
	public String getName();
	
	public String createPublicLink(String url);
	
}
