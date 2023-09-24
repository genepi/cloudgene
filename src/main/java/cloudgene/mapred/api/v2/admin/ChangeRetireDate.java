package cloudgene.mapred.api.v2.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.BaseResource;

public class ChangeRetireDate extends BaseResource {

	private static final Log log = LogFactory.getLog(ChangeRetireDate.class);

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

		int days = 0;
		try {
			days = Integer.parseInt(getAttribute("days"));
		} catch (Exception e) {
			return error400("The provided number value '" + getAttribute("days") + "' is not an integer.");
		}

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
				|| job.getState() == AbstractJob.STATE_FAILED_AND_NOTIFICATION_SEND) {

			try {

				job.setDeletedOn(job.getDeletedOn() + (days * 24 * 60 * 60 * 1000));

				dao.update(job);

				log.info(String.format("Job: Extended retire date for job %s (by ADMIN user ID %s - email %s)", job.getId(), user.getId(), user.getMail()));

				return new StringRepresentation("Update delete on date for job " + job.getId() + ".");

			} catch (Exception e) {

				return new StringRepresentation("Update delete date for job " + job.getId() + " failed.");
			}

		} else {
			return new StringRepresentation("Job " + jobId + " has wrong state for this operation.");
		}

	}
}
