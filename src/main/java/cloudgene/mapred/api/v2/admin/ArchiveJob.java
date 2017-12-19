package cloudgene.mapred.api.v2.admin;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.Settings;
import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;

public class ArchiveJob extends BaseResource {

	@Get
	public Representation get() {

		User user = getAuthUser();

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires user authentication.");

		}

		if (!user.isAdmin()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires administration rights.");
		}

		Settings settings = getSettings();

		String jobId = getAttribute("job");

		JobDao dao = new JobDao(getDatabase());

		if (jobId == null) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation("no job id found.");
		}

		AbstractJob job = dao.findById(jobId);

		if (job == null) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation("Job " + jobId + " not found.");
		}

		if (job.getState() == AbstractJob.STATE_SUCCESS || job.getState() == AbstractJob.STATE_FAILED
				|| job.getState() == AbstractJob.STATE_CANCELED) {

			try {

				// delete local directory and hdfs directory
				String localOutput = FileUtil.path(settings.getLocalWorkspace(), job.getId());

				String hdfsOutput = HdfsUtil.makeAbsolute(HdfsUtil.path(settings.getHdfsWorkspace(), job.getId()));

				FileUtil.deleteDirectory(localOutput);
				HdfsUtil.delete(hdfsOutput);

				job.setState(AbstractJob.STATE_RETIRED);
				dao.update(job);

				return new StringRepresentation("Retired job " + jobId);

			} catch (Exception e) {

				return new StringRepresentation("Retire " + job.getId() + " failed.");
			}

		} else {

			return new StringRepresentation("Job " + jobId + " has wrong state for this operation.");
		}

	}
}
