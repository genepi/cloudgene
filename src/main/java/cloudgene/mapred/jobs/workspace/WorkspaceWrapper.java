package cloudgene.mapred.jobs.workspace;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import cloudgene.sdk.internal.IExternalWorkspace;

public class WorkspaceWrapper implements IExternalWorkspace {

	private cloudgene.mapred.jobs.workspace.IExternalWorkspace newWorkspace;

	public WorkspaceWrapper(cloudgene.mapred.jobs.workspace.IExternalWorkspace newWorkspace) {
		this.newWorkspace = newWorkspace;
	}

	@Override
	public String createPublicLink(String arg0) {
		return newWorkspace.createPublicLink(arg0);
	}

	@Override
	public void delete(String arg0) throws IOException {
		newWorkspace.delete(arg0);
	}

	@Override
	public InputStream download(String arg0) throws IOException {
		return newWorkspace.download(arg0);
	}

	@Override
	public String getName() {
		return newWorkspace.getName();
	}

	@Override
	public void setup(String arg0) throws IOException {
		newWorkspace.setup(arg0);
	}

	@Override
	public String upload(String arg0, File arg1) throws IOException {
		return newWorkspace.upload(arg0, arg1);
	}

}
