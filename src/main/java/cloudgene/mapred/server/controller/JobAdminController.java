package cloudgene.mapred.server.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.server.auth.AuthenticationService;
import cloudgene.mapred.server.responses.JobAdminResponse;
import cloudgene.mapred.server.responses.JobResponse;
import cloudgene.mapred.server.responses.MessageResponse;
import cloudgene.mapred.server.services.JobCleanUpService;
import cloudgene.mapred.server.services.JobService;
import cloudgene.mapred.util.FormUtil;
import cloudgene.mapred.util.Settings;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import jakarta.inject.Inject;

@Controller("/api/v2/admin/jobs")
@Secured(User.ROLE_ADMIN)

public class JobAdminController {

	private static Logger log = LoggerFactory.getLogger(JobAdminController.class);

	public static final int DEFAULT_PAGE_SIZE = 15;

	public static final long HIGH_PRIORITY = 0;

	@Inject
	protected cloudgene.mapred.server.Application application;

	@Inject
	protected JobService jobService;

	@Inject
	protected AuthenticationService authenticationService;

	@Inject
	protected JobCleanUpService cleanUpService;

	@Inject
	protected FormUtil formUtil;

	@Get("/{id}/reset")
	public MessageResponse reset(Authentication authentication, String id, @Nullable @QueryValue("max") Integer max) {

		User admin = authenticationService.getUserByAuthentication(authentication);

		int maxDownloads = application.getSettings().getMaxDownloads();
		if (max != null) {
			maxDownloads = max;
		}

		AbstractJob job = jobService.getById(id);
		int count = jobService.reset(job, maxDownloads);

		log.info(String.format("Job: Resetting download counters for job %s (by ADMIN user ID %s - email %s)",
				job.getId(), admin.getId(), admin.getMail()));

		return MessageResponse.success(id + ": counter of " + count + " downloads reset to " + maxDownloads);
	}

	@Get("/{id}/retire")
	public MessageResponse retire(Authentication authentication, String id) {

		User admin = authenticationService.getUserByAuthentication(authentication);

		Settings settings = application.getSettings();
		int days = settings.getRetireAfter() - settings.getNotificationAfter();
		AbstractJob job = jobService.getById(id);
		String message = cleanUpService.sendNotification(job, days);

		log.info(String.format("Job: Set retire date for job %s (by ADMIN user ID %s - email %s)", job.getId(),
				admin.getId(), admin.getMail()));

		return MessageResponse.success(message);

	}

	@Get("/{id}/priority")
	public MessageResponse changePriority(Authentication authentication, String id) {

		User admin = authenticationService.getUserByAuthentication(authentication);

		AbstractJob job = jobService.getById(id);
		jobService.changePriority(job, HIGH_PRIORITY);

		log.info(String.format("Job: Update priority for job %s (by ADMIN user ID %s - email %s)", job.getId(),
				admin.getId(), admin.getMail()));

		return MessageResponse.success("Update priority for job " + job.getId() + ".");

	}

	@Get("/{id}/archive")
	public MessageResponse archive(Authentication authentication, String id) {

		User admin = authenticationService.getUserByAuthentication(authentication);

		AbstractJob job = jobService.getById(id);
		String message = jobService.archive(job);

		log.info(String.format("Job: Immediately retired job %s (by ADMIN user ID %s - email %s)", job.getId(),
				admin.getId(), admin.getMail()));

		return MessageResponse.success(message);

	}

	@Get("/{id}/change-retire/{days}")
	@Produces(MediaType.TEXT_PLAIN)
	public MessageResponse increaseRetireDate(Authentication authentication, String id, Integer days) {

		User admin = authenticationService.getUserByAuthentication(authentication);

		AbstractJob job = jobService.getById(id);
		String message = jobService.increaseRetireDate(job, days);

		log.info(String.format("Job: Extended retire date for job %s (by ADMIN user ID %s - email %s)", job.getId(),
				admin.getId(), admin.getMail()));

		return MessageResponse.success(message);

	}

	@Get("/retire")
	@Produces(MediaType.TEXT_PLAIN)
	public String retireJobs(Authentication authentication) {

		User admin = authenticationService.getUserByAuthentication(authentication);

		int notifications = cleanUpService.sendNotifications();
		int retired = cleanUpService.executeRetire();

		log.info(String.format("Job: Manually triggered retiring of all eligible jobs (by ADMIN user ID %s - email %s)",
				admin.getId(), admin.getMail()));

		return "NotificationJob:\n" + notifications + " notifications sent." + "\n\nRetireJob:\n" + retired
				+ " jobs retired.";

	}

	@Get("/")
	public JobAdminResponse getJobs(Authentication authentication, @Nullable @QueryValue("state") String state) {

		User admin = authenticationService.getUserByAuthentication(authentication);

		List<AbstractJob> jobs = jobService.getJobs(state);
		List<JobResponse> responses = JobResponse.build(jobs, admin);
		String workspace = application.getSettings().getLocalWorkspace();

		log.info(String.format("Job: list all jobs of of all users (by ADMIN user ID %s - email %s)", admin.getId(),
				admin.getMail()));
		
		return JobAdminResponse.build(jobs, responses, workspace);

	}

}
