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

public class HotRetireJob extends BaseResource {

	@Get
	public Representation get() {

		User user = getAuthUser();

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");

		}

		if (!user.isAdmin()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires administration rights.");
		}

		Settings settings = getSettings();

		String jobId = getAttribute("job");

		JobDao dao = new JobDao(getDatabase());

		int days = settings.getRetireAfter() - settings.getNotificationAfter();

		if (jobId == null) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation("no job id found.");
		}

		AbstractJob job = dao.findById(jobId);

		if (job == null) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation("Job " + jobId + " not found.");
		}

		if (job.getState() == AbstractJob.STATE_SUCCESS) {

			try {

				String subject = "[" + settings.getName() + "] Job "
						+ job.getId() + " will be retired in " + days
						+ " days";

				String body = getWebApp().getTemplate(Template.RETIRE_JOB_MAIL,
						job.getUser().getFullName(), days, job.getId());

				if (!job.getUser().getUsername().equals("public")) {

					MailUtil.send(settings, job.getUser().getMail(), subject,
							body);

				}

				job.setState(AbstractJob.STATE_SUCESS_AND_NOTIFICATION_SEND);
				job.setDeletedOn(System.currentTimeMillis()
						+ ((settings.getRetireAfterInSec() - settings
								.getNotificationAfterInSec()) * 1000));
				dao.update(job);

				return new StringRepresentation("Sent notification for job "
						+ job.getId() + ".");

			} catch (Exception e) {

				return new StringRepresentation("Sent notification for job "
						+ job.getId() + " failed.");
			}

		} else if (job.getState() == AbstractJob.STATE_FAILED
				|| job.getState() == AbstractJob.STATE_CANCELED) {
			job.setState(AbstractJob.STATE_FAILED_AND_NOTIFICATION_SEND);
			job.setDeletedOn(System.currentTimeMillis()
					+ ((settings.getRetireAfterInSec() - settings
							.getNotificationAfterInSec()) * 1000));
			dao.update(job);

			return new StringRepresentation(jobId
					+ ": delete date set. job failed, no notification sent.");

		} else {

			return new StringRepresentation("Job " + jobId
					+ " has wrong state for this operation.");
		}

	}
}
