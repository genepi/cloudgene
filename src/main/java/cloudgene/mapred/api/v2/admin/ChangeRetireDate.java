package cloudgene.mapred.api.v2.admin;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.MailUtil;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.util.Template;

public class ChangeRetireDate extends BaseResource {

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

		String jobId = getAttribute("job");

		int days = Integer.parseInt(getAttribute("days"));

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

		if (job.getState() == AbstractJob.STATE_SUCESS_AND_NOTIFICATION_SEND
				|| job.getState() == AbstractJob.STATE_SUCESS_AND_NOTIFICATION_SEND) {

			try {

				job.setDeletedOn(job.getDeletedOn() + (days * 24 * 60 * 60));

				dao.update(job);

				return new StringRepresentation("Update delete on date for job " + job.getId() + ".");

			} catch (Exception e) {

				return new StringRepresentation("Update delete date for job " + job.getId() + " failed.");
			}

		} else {
			return new StringRepresentation("Job " + jobId + " has wrong state for this operation.");
		}

	}
}
