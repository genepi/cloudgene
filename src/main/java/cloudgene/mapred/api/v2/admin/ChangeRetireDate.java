package cloudgene.mapred.api.v2.admin;

import javax.validation.constraints.NotBlank;

import cloudgene.mapred.Application;
import cloudgene.mapred.auth.AuthenticationService;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.exceptions.JsonHttpStatusException;
import cloudgene.mapred.jobs.AbstractJob;
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
public class ChangeRetireDate {

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Get("/api/v2/admin/jobs/{jobId}/change-retire/{days}")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	@Produces(MediaType.TEXT_PLAIN)
	public String get(Authentication authentication, @PathVariable @NotBlank String jobId,
			@PathVariable @NotBlank String days) {

		User user = authenticationService.getUserByAuthentication(authentication);

		if (!user.isAdmin()) {
			throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "The request requires administration rights.");
		}

		int amountDays;
		try {
			amountDays = Integer.parseInt(days);
		} catch (Exception e) {
			throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST,
					"The provided number value '" + days + "' is not an integer.");
		}

		JobDao dao = new JobDao(application.getDatabase());

		AbstractJob job = dao.findById(jobId);

		if (job == null) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, "Job " + jobId + " not found.");
		}

		if (job.getState() == AbstractJob.STATE_SUCESS_AND_NOTIFICATION_SEND
				|| job.getState() == AbstractJob.STATE_FAILED_AND_NOTIFICATION_SEND) {

			try {

				job.setDeletedOn(job.getDeletedOn() + (amountDays * 24 * 60 * 60 * 1000));

				dao.update(job);

				return "Update delete on date for job " + job.getId() + ".";

			} catch (Exception e) {

				return "Update delete date for job " + job.getId() + " failed.";
			}

		} else {
			return "Job " + jobId + " has wrong state for this operation.";
		}

	}
}
