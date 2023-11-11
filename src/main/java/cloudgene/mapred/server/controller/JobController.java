package cloudgene.mapred.server.controller;

import java.util.List;
import java.util.function.Function;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.server.auth.AuthenticationService;
import cloudgene.mapred.server.auth.AuthenticationType;
import cloudgene.mapred.server.exceptions.JsonHttpStatusException;
import cloudgene.mapred.server.responses.JobResponse;
import cloudgene.mapred.server.responses.MessageResponse;
import cloudgene.mapred.server.responses.PageResponse;
import cloudgene.mapred.server.responses.ResponseObject;
import cloudgene.mapred.server.services.JobService;
import cloudgene.mapred.util.FormUtil;
import cloudgene.mapred.util.FormUtil.Parameter;
import cloudgene.mapred.util.Page;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.server.multipart.MultipartBody;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

@Controller("/api/v2/jobs")
public class JobController {

	private static Logger log = LoggerFactory.getLogger(JobController.class);

	private static final String MESSAGE_JOB_RESTARTED = "Your job was successfully added to the job queue.";

	public static final int DEFAULT_PAGE_SIZE = 15;

	public static final long HIGH_PRIORITY = 0;

	@Inject
	protected cloudgene.mapred.server.Application application;

	@Inject
	protected JobService jobService;

	@Inject
	protected AuthenticationService authenticationService;

	@Inject
	protected FormUtil formUtil;

	@Get("/{id}")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public JobResponse get(Authentication authentication, String id) {

		User user = authenticationService.getUserByAuthentication(authentication, AuthenticationType.ALL_TOKENS);

		blockInMaintenanceMode(user);

		AbstractJob job = jobService.getByIdAndUser(id, user);

		String message = String.format("Job: Get details for job ID %s", job.getId());
		if (user.isAdmin()) {
			message += String.format(" (by ADMIN user ID %s - email %s)", user.getId(), user.getMail());
		}
		log.info(message);

		JobResponse response = JobResponse.build(job, user);
		return response;
	}

	@Post("/submit/{app}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public Publisher<HttpResponse<Object>> submit(Authentication authentication, String app, @Body MultipartBody body) {

		return formUtil.processMultipartBody(body, new Function<List<Parameter>, HttpResponse<Object>>() {

			@Override
			public HttpResponse<Object> apply(List<Parameter> form) {

				User user = authenticationService.getUserByAuthentication(authentication,
						AuthenticationType.ALL_TOKENS);

				try {

					blockInMaintenanceMode(user);

					AbstractJob job = jobService.submitJob(app, form, user);

					String message = String.format("Job: Created job ID %s for user %s (ID %s - email %s)", user.getId(),
							user.getUsername(), user.getId(), user.getMail());
					if (user.isAccessedByApi()) {
						message += " (via API token)";
					}
					log.info(message);

					message = "Your job was successfully added to the job queue.";
					return HttpResponse.ok(ResponseObject.build(job.getId(), message, true));
				} catch (JsonHttpStatusException e) {
					return HttpResponse.status(e.getStatus()).body(e.getObject());
				}
			}
		});
	}


	@Get("/")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public PageResponse list(Authentication authentication, @QueryValue @Nullable Integer page) {

		User user = authenticationService.getUserByAuthentication(authentication, AuthenticationType.ALL_TOKENS);
		blockInMaintenanceMode(user);

		Page<AbstractJob> jobs = jobService.getAllByUserAndPage(user, page, DEFAULT_PAGE_SIZE);

		List<JobResponse> responses = JobResponse.build(jobs.getData(), user);
		return PageResponse.build(jobs, responses);
	}

	@Delete("/{id}")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public JobResponse delete(Authentication authentication, String id) {

		User user = authenticationService.getUserByAuthentication(authentication);
		blockInMaintenanceMode(user);

		AbstractJob job = jobService.getByIdAndUser(id, user);
		jobService.delete(job);

		String message = String.format("Job: Deleted job ID %s", job.getId());
		if (user.isAdmin()) {
			message += String.format(" (by ADMIN user ID %s - email %s)", user.getId(), user.getMail());
		}
		log.info(message);

		JobResponse response = JobResponse.build(job, user);

		return response;

	}

	@Get("/{id}/status")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public JobResponse status(Authentication authentication, String id) {

		User user = authenticationService.getUserByAuthentication(authentication, AuthenticationType.ALL_TOKENS);
		blockInMaintenanceMode(user);

		AbstractJob job = jobService.getByIdAndUser(id, user);
		JobResponse response = JobResponse.build(job, user);
		return response;

	}

	@Get("/{id}/cancel")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public JobResponse cancel(Authentication authentication, String id) {

		User user = authenticationService.getUserByAuthentication(authentication, AuthenticationType.ALL_TOKENS);
		blockInMaintenanceMode(user);

		AbstractJob job = jobService.getByIdAndUser(id, user);
		jobService.cancel(job);

		String message = String.format("Job: Canceled job ID %s", job.getId());
		if (user.isAdmin()) {
			message += String.format(" (by ADMIN user ID %s - email %s)", user.getId(), user.getMail());
		}
		log.info(message);

		JobResponse response = JobResponse.build(job, user);
		return response;

	}

	@Get("/{id}/restart")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public MessageResponse restart(Authentication authentication, String id) {

		User user = authenticationService.getUserByAuthentication(authentication, AuthenticationType.ALL_TOKENS);
		blockInMaintenanceMode(user);

		AbstractJob job = jobService.getByIdAndUser(id, user);
		jobService.restart(job);

		String message = String.format("Job: Restarted job ID %s", job.getId());
		if (user.isAdmin()) {
			message += String.format(" (by ADMIN user ID %s - email %s)", user.getId(), user.getMail());
		}
		log.info(message);

		return MessageResponse.success(MESSAGE_JOB_RESTARTED);
	}

	private void blockInMaintenanceMode(User user) {
		if (application.getSettings().isMaintenance() && !user.isAdmin()) {
			throw new JsonHttpStatusException(HttpStatus.SERVICE_UNAVAILABLE,
					"This functionality is currently under maintenance.");
		}
	}

}
