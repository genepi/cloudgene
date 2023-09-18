package cloudgene.mapred.jobs.workspace;

import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.util.Settings;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class WorkspaceFactory {

	@Inject
	protected Application application;

	public IWorkspace getDefault() {

		Settings settings = application.getSettings();

		String type = settings.getExternalWorkspaceType();

		if (type == null) {
			return new LocalWorkspace(settings.getLocalWorkspace());
		}

		if (type.equalsIgnoreCase("S3")) {
			String bucket = settings.getExternalWorkspaceLocation();
			return new S3Workspace(bucket);
		}

		return new LocalWorkspace(settings.getLocalWorkspace());

	}

	public IWorkspace getByUrl(String url) {

		Settings settings = application.getSettings();

		if (url == null || url.isEmpty()) {
			throw new RuntimeException("Workspace type could not determined for empty url.");
		}

		if (url.startsWith("s3://")) {
			String bucket = settings.getExternalWorkspaceLocation();
			return new S3Workspace(bucket);
		}

		return new LocalWorkspace(settings.getLocalWorkspace());

	}

	public IWorkspace getByJob(AbstractJob job) {
		IWorkspace workspace = getDefault();
		workspace.setJob(job.getId());
		return workspace;
	}

}
