package cloudgene.mapred.api.v2.jobs;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.PublicUser;
import genepi.io.FileUtil;

public class GetChunk extends BaseResource {

	@Get
	public Representation get() {
		try {
			String jobId = getAttribute("job");
			String filename = getAttribute("filename");

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

			User user = getAuthUser(false);

			// public mode
			if (user == null) {
				user = PublicUser.getUser(getDatabase());
			}

			if (!user.isAdmin() && job.getUser().getId() != user.getId()) {
				return error403("Access denied.");
			}

			String resultFile = FileUtil.path(getSettings().getLocalWorkspace(), job.getId(), "chunks", filename);

			return new FileRepresentation(resultFile, MediaType.TEXT_HTML);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return error(Status.CLIENT_ERROR_BAD_REQUEST, "oje");
	}

}
