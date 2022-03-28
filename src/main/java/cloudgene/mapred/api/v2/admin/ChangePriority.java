package cloudgene.mapred.api.v2.admin;

import javax.validation.constraints.NotBlank;

import cloudgene.mapred.Application;
import cloudgene.mapred.auth.AuthenticationService;
import cloudgene.mapred.core.User;
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
public class ChangePriority  {

	public static final long HIGH_PRIORITY = 0;

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Get("/api/v2/admin/jobs/{jobId}/priority")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	@Produces(MediaType.TEXT_PLAIN)
	public String get(Authentication authentication, @PathVariable @NotBlank String jobId) {

		User user = authenticationService.getUserByAuthentication(authentication);

		if (!user.isAdmin()) {
			throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "The request requires administration rights.");
		}


		AbstractJob job = application.getWorkflowEngine().getJobById(jobId);

		if (job == null) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, "Job " + jobId + " not found.");
		}

		application.getWorkflowEngine().updatePriority(job, HIGH_PRIORITY);

		return "Update priority for job " + job.getId() + ".";
	}
}
