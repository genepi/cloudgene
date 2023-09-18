package cloudgene.mapred.server.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.workspace.WorkspaceFactory;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.auth.AuthenticationService;
import cloudgene.mapred.server.auth.AuthenticationType;
import cloudgene.mapred.server.services.JobService;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

@Controller
public class LogController {

	private static Logger log = LoggerFactory.getLogger(LogController.class);

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Inject
	protected JobService jobService;

	@Inject
	protected WorkspaceFactory workspaceFactory;

	@Get("/logs/{id}")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	@Produces(MediaType.TEXT_PLAIN)
	public String getByJob(Authentication authentication, String id) throws IOException {

		User user = authenticationService.getUserByAuthentication(authentication, AuthenticationType.ALL_TOKENS);
		AbstractJob job = jobService.getByIdAndUser(id, user);

		String logContent = jobService.getJobLog(job, AbstractJob.JOB_LOG);
		String outputContent = jobService.getJobLog(job, AbstractJob.JOB_OUT);
		
		StringBuffer buffer = new StringBuffer();
		if (!logContent.isEmpty()) {
			buffer.append("job.txt:\n\n");
			buffer.append(logContent);
		}

		if (!outputContent.isEmpty()) {
			buffer.append("\n\nstd.out:\n\n");
			buffer.append(outputContent);
		}

		String message = String.format("Job: viewing logs for job ID %s", job.getId());
		if (user.isAdmin()) {
			message += String.format(" (by ADMIN user ID %s - email %s)", user.getId(), user.getMail());
		}
		log.info(message);

		return buffer.toString();

	}
}
