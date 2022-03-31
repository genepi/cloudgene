package cloudgene.mapred.api.v2.admin;

import javax.validation.constraints.NotBlank;

import cloudgene.mapred.core.Template;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.auth.AuthenticationService;
import cloudgene.mapred.server.exceptions.JsonHttpStatusException;
import cloudgene.mapred.util.MailUtil;
import cloudgene.mapred.util.Settings;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

@Controller
public class HotRetireJob {

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Get("/api/v2/admin/jobs/{jobId}/retire")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	@Produces(MediaType.TEXT_PLAIN)
	public String get(Authentication authentication, @PathVariable @NotBlank String jobId) {

		User user = authenticationService.getUserByAuthentication(authentication);

		if (!user.isAdmin()) {
			throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "The request requires administration rights.");
		}

		Settings settings = application.getSettings();

		JobDao dao = new JobDao(application.getDatabase());

		int days = settings.getRetireAfter() - settings.getNotificationAfter();

		AbstractJob job = dao.findById(jobId);

		if (job == null) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, "Job " + jobId + " not found.");
		}

		if (job.getState() == AbstractJob.STATE_SUCCESS) {

			try {

				String subject = "[" + settings.getName() + "] Job " + job.getId() + " will be retired in " + days
						+ " days";

				String body = application.getTemplate(Template.RETIRE_JOB_MAIL, job.getUser().getFullName(), days,
						job.getId());

				if (!job.getUser().getUsername().equals("public")) {

					MailUtil.send(settings, job.getUser().getMail(), subject, body);

				}

				job.setState(AbstractJob.STATE_SUCESS_AND_NOTIFICATION_SEND);
				job.setDeletedOn(System.currentTimeMillis()
						+ ((settings.getRetireAfterInSec() - settings.getNotificationAfterInSec()) * 1000));
				dao.update(job);

				return "Sent notification for job " + job.getId() + ".";

			} catch (Exception e) {

				return "Sent notification for job " + job.getId() + " failed.";
			}

		} else if (job.getState() == AbstractJob.STATE_FAILED || job.getState() == AbstractJob.STATE_CANCELED) {
			job.setState(AbstractJob.STATE_FAILED_AND_NOTIFICATION_SEND);
			job.setDeletedOn(System.currentTimeMillis()
					+ ((settings.getRetireAfterInSec() - settings.getNotificationAfterInSec()) * 1000));
			dao.update(job);

			return jobId + ": delete date set. job failed, no notification sent.";

		} else {

			return "Job " + jobId + " has wrong state for this operation.";
		}

	}
}
