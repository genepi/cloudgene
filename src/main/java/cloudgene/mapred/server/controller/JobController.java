package cloudgene.mapred.server.controller;

import java.util.List;
import java.util.Vector;
import java.util.function.Function;

import org.reactivestreams.Publisher;

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
import cloudgene.mapred.util.Settings;
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
import io.micronaut.http.annotation.Produces;
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

		return object.toString();

	}

	@Get("/{id}/restart")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public MessageResponse restart(Authentication authentication, String id) {

		User user = authenticationService.getUserByAuthentication(authentication, AuthenticationType.ALL_TOKENS);
		blockInMaintenanceMode(user);

		AbstractJob job = jobService.getByIdAndUser(id, user);
		jobService.restart(job);

		return MessageResponse.success(MESSAGE_JOB_RESTARTED);
	}

	@Get("/{id}/reset")
	@Secured(User.ROLE_ADMIN)
	public MessageResponse reset(String id, @Nullable @QueryValue("max") Integer max) {

		int maxDownloads = application.getSettings().getMaxDownloads();
		if (max != null) {
			maxDownloads = max;
		}

		AbstractJob job = jobService.getById(id);
		int count = jobService.reset(job, maxDownloads);

		return MessageResponse.success(id + ": counter of " + count + " downloads reset to " + maxDownloads);
	}

	@Get("/{id}/retire")
	@Secured(User.ROLE_ADMIN)
	public MessageResponse retire(String id) {

		Settings settings = application.getSettings();
		int days = settings.getRetireAfter() - settings.getNotificationAfter();
		AbstractJob job = jobService.getById(id);
		String message = jobService.retire(job, days);
		return MessageResponse.success(message);

	}

	@Get("/{id}/priority")
	@Secured(User.ROLE_ADMIN)
	public MessageResponse changePriority(String id) {

		AbstractJob job = jobService.getById(id);
		jobService.changePriority(job, HIGH_PRIORITY);
		return MessageResponse.success("Update priority for job " + job.getId() + ".");

	}

	@Get("/{id}/archive")
	@Secured(User.ROLE_ADMIN)
	public MessageResponse archive(String id) {

		AbstractJob job = jobService.getById(id);
		String message = jobService.archive(job);
		return MessageResponse.success(message);

	}

	@Get("/{id}/change-retire/{days}")
	@Secured(User.ROLE_ADMIN)
	@Produces(MediaType.TEXT_PLAIN)
	public MessageResponse increaseRetireDate(String id, Integer days) {

		AbstractJob job = jobService.getById(id);
		String message = jobService.increaseRetireDate(job, days);
		return MessageResponse.success(message);

	}

	private void blockInMaintenanceMode(User user) {
		if (application.getSettings().isMaintenance() && !user.isAdmin()) {
			throw new JsonHttpStatusException(HttpStatus.SERVICE_UNAVAILABLE,
					"This functionality is currently under maintenance.");
		}
	}

}
