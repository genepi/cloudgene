package cloudgene.mapred.server.controller;

import java.util.List;
import java.util.Vector;
import java.util.function.Function;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneParameterOutput;
import cloudgene.mapred.server.auth.AuthenticationService;
import cloudgene.mapred.server.auth.AuthenticationType;
import cloudgene.mapred.server.exceptions.JsonHttpStatusException;
import cloudgene.mapred.server.responses.MessageResponse;
import cloudgene.mapred.server.services.JobService;
import cloudgene.mapred.util.FormUtil;
import cloudgene.mapred.util.FormUtil.Parameter;
import cloudgene.mapred.util.JSONConverter;
import cloudgene.mapred.util.Page;
import cloudgene.mapred.util.PageUtil;
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
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

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
	public String get(Authentication authentication, String id) {

		User user = authenticationService.getUserByAuthentication(authentication, AuthenticationType.ALL_TOKENS);

		blockInMaintenanceMode(user);

		AbstractJob job = jobService.getByIdAndUser(id, user);

		// TODO: move this logic to JobResponse
		// removes outputs that are for admin only
		List<CloudgeneParameterOutput> adminParams = new Vector<>();
		if (!user.isAdmin()) {
			for (CloudgeneParameterOutput param : job.getOutputParams()) {
				if (param.isAdminOnly()) {
					adminParams.add(param);
				}
			}
		}

		// remove all outputs that are not downloadable
		for (CloudgeneParameterOutput param : job.getOutputParams()) {
			if (!param.isDownload()) {
				adminParams.add(param);
			}
		}
		job.getOutputParams().removeAll(adminParams);

		// set log if user is admin
		if (user.isAdmin()) {
			job.setLogs("logs/" + job.getId());
		}

		JSONObject object = JSONConverter.convert(job);
		object.put("username", job.getUser().getUsername());
		
		String message = String.format("Job: Get details for job ID %s", job.getId());
		if (user.isAdmin()) {
			message += String.format(" (by ADMIN user ID %s - email %s)", user.getId(), user.getMail());
		}
		log.info(message);
		
		return object.toString();
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
				blockInMaintenanceMode(user);
				
				AbstractJob job = jobService.submitJob(app, form, user);

				// TODO: create response object or add custom properties to MessageResponse?
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("success", true);
				jsonObject.put("message", "Your job was successfully added to the job queue.");
				jsonObject.put("id", job.getId());

				String message = String.format("Job: Created job ID %s for user %s (ID %s - email %s)", user.getId(), user.getUsername(), user.getId(), user.getMail());
				if (user.isAccessedByApi()) {
					message += " (via API token)";
				}
				log.info(message);
				
				return HttpResponse.ok(jsonObject.toString());

			}
		});

	}

	@Get("/")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public String list(Authentication authentication, @QueryValue @Nullable Integer page) {

		User user = authenticationService.getUserByAuthentication(authentication, AuthenticationType.ALL_TOKENS);
		blockInMaintenanceMode(user);

		Page<AbstractJob> jobs = jobService.getAllByUserAndPage(user, page, DEFAULT_PAGE_SIZE);

		// TODO: move into JobResponse object
		JsonConfig config = new JsonConfig();
		config.setExcludes(new String[] { "user", "outputParams", "inputParams", "output", "error", "s3Url", "task",
				"config", "mapReduceJob", "job", "step", "context", "hdfsWorkspace", "localWorkspace", "logOutFiles",
				"logs", "removeHdfsWorkspace", "settings", "setupComplete", "stdOutFile", "steps", "workingDirectory",
				"map", "reduce", "logOutFile", "deletedOn", "applicationId", "running" });

		JSONObject object = PageUtil.createPageObject(jobs);

		JSONArray jsonArray = JSONArray.fromObject(jobs.getData(), config);
		object.put("data", jsonArray);

		return object.toString();
	}

	@Delete("/{id}")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public String delete(Authentication authentication, String id) {

		User user = authenticationService.getUserByAuthentication(authentication);
		blockInMaintenanceMode(user);

		AbstractJob job = jobService.getByIdAndUser(id, user);
		jobService.delete(job);
		JSONObject object = JSONConverter.convert(job);
		
		String message = String.format("Job: Deleted job ID %s", job.getId());
		if (user.isAdmin()) {
			message += String.format(" (by ADMIN user ID %s - email %s)", user.getId(), user.getMail());
		}
		log.info(message);
		
		return object.toString();

	}

	@Get("/{id}/status")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public String status(Authentication authentication, String id) {

		User user = authenticationService.getUserByAuthentication(authentication, AuthenticationType.ALL_TOKENS);
		blockInMaintenanceMode(user);

		AbstractJob job = jobService.getByIdAndUser(id, user);
		JSONObject object = JSONConverter.convert(job);

		return object.toString();

	}

	@Get("/{id}/cancel")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public String cancel(Authentication authentication, String id) {

		User user = authenticationService.getUserByAuthentication(authentication, AuthenticationType.ALL_TOKENS);
		blockInMaintenanceMode(user);

		AbstractJob job = jobService.getByIdAndUser(id, user);
		jobService.cancel(job);

		JSONObject object = JSONConverter.convert(job);

		String message = String.format("Job: Canceled job ID %s", job.getId());
		if (user.isAdmin()) {
			message += String.format(" (by ADMIN user ID %s - email %s)", user.getId(), user.getMail());
		}
		log.info(message);
		
		return object.toString();

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
