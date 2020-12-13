package cloudgene.mapred.api.v2.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.data.MediaType;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.DownloadDao;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneParameterOutput;
import cloudgene.mapred.jobs.Download;
import cloudgene.mapred.jobs.workspace.ExternalWorkspaceFactory;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.PublicUser;
import cloudgene.sdk.internal.IExternalWorkspace;
import genepi.io.FileUtil;

public class ExternalResults extends BaseResource {

	private static final Log log = LogFactory.getLog(ExternalResults.class);

	@Get
	public Representation get() {

		String jobId = (String) getRequest().getAttributes().get("job");
		String hash = (String) getRequest().getAttributes().get("hash");
		String filename = (String) getRequest().getAttributes().get("filename");

		JobDao jobDao = new JobDao(getDatabase());
		AbstractJob job = jobDao.findById(jobId);

		if (job == null) {
			return error404("Job " + jobId + " not found.");
		}

		// job is running -> load it from queue
		if (job.getState() == AbstractJob.STATE_WAITING || job.getState() == AbstractJob.STATE_RUNNING
				|| job.getState() == AbstractJob.STATE_EXPORTING) {
			job = getWorkflowEngine().getJobById(jobId);
		}

		User user = getAuthUserAndAllowApiToken(false);

		// public mode
		if (user == null) {
			user = PublicUser.getUser(getDatabase());
		}

		if (!user.isAdmin() && job.getUser().getId() != user.getId()) {
			return error403("Access denied.");
		}
		
		DownloadDao dao = new DownloadDao(getDatabase());
		Download download = dao.findByHash(hash);

		// job is running and not in database --> download possible of
		// autoexport params
		if (download == null) {
			for (CloudgeneParameterOutput param : job.getOutputParams()) {
				if (param.isAutoExport() && param.getFiles() != null) {
					for (Download download2 : param.getFiles()) {
						if (download2.getHash().equals(hash)) {
							download = download2;
						}
					}
				}
			}
		}
		
		if (download == null) {
			return error404("download not found.");
		}

		if (!download.getName().endsWith(filename)) {
			return error404("download not found.");
		}

		if (download.getCount() == 0) {
			return error400("number of max downloads exceeded.");
		}

		// update download counter if it not set to unlimited
		if (download.getCount() != -1) {
			download.decCount();
			dao.update(download);
		}

		IExternalWorkspace externalWorkspace = ExternalWorkspaceFactory.get(download.getPath());
		if (externalWorkspace != null) {
			// external workspace found, use link method and create redirect response
			String publicUrl = externalWorkspace.createPublicLink(download.getPath());
			redirectTemporary(publicUrl);
			return new StringRepresentation(publicUrl);
		} else {
			// no external workspace found, use local workspace
			String localWorkspace = getSettings().getLocalWorkspace();
			String resultFile = FileUtil.path(localWorkspace, download.getPath());
			log.debug("Downloading file from local workspace " + resultFile);
			MediaType mediaType = DownloadResults.getMediaType(download.getPath());
			return new FileRepresentation(resultFile, mediaType);
		}

	}

}
