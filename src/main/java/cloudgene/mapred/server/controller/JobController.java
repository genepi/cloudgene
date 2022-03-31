package cloudgene.mapred.server.controller;

import java.util.List;
import java.util.Vector;

import javax.validation.constraints.NotBlank;

import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneParameterOutput;
import cloudgene.mapred.server.auth.AuthenticationService;
import cloudgene.mapred.server.auth.AuthenticationType;
import cloudgene.mapred.server.exceptions.JsonHttpStatusException;
import cloudgene.mapred.server.responses.MessageResponse;
import cloudgene.mapred.server.services.JobService;
import cloudgene.mapred.util.JSONConverter;
import cloudgene.mapred.util.PublicUser;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import net.sf.json.JSONObject;

@Controller("/api/v2/jobs")
public class JobController {

	private static final String MESSAGE_JOB_RESTARTED = "Your job was successfully added to the job queue.";

	@Inject
	protected cloudgene.mapred.server.Application application;

	@Inject
	protected JobService jobService;

	@Inject
	protected AuthenticationService authenticationService;

	@Get("/{id}")
	@Secured(SecurityRule.IS_ANONYMOUS)
	public String get(@PathVariable @NotBlank String id, @Nullable Authentication authentication) {

		User user = authenticationService.getUserByAuthentication(authentication, AuthenticationType.ALL_TOKENS);

		if (user == null) {
			user = PublicUser.getUser(application.getDatabase());
		}

		if (application.getSettings().isMaintenance() && !user.isAdmin()) {
			throw new JsonHttpStatusException(HttpStatus.SERVICE_UNAVAILABLE,
					"This functionality is currently under maintenance.");
		}

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

	@Delete("/{id}")
	@Secured(SecurityRule.IS_ANONYMOUS)
	public String delete(@PathVariable @NotBlank String id, @Nullable Authentication authentication) {

		User user = authenticationService.getUserByAuthentication(authentication);

		if (user == null) {
			user = PublicUser.getUser(application.getDatabase());
		}

		if (application.getSettings().isMaintenance() && !user.isAdmin()) {
			throw new JsonHttpStatusException(HttpStatus.SERVICE_UNAVAILABLE,
					"This functionality is currently under maintenance.");
		}

		AbstractJob job = jobService.getByIdAndUser(id, user);
		jobService.delete(job);
		JSONObject object = JSONConverter.convert(job);

		return object.toString();

	}

	@Get("/{id}/status")
	@Secured(SecurityRule.IS_ANONYMOUS)
	public String status(String id, @Nullable Authentication authentication) {

		User user = authenticationService.getUserByAuthentication(authentication, AuthenticationType.ALL_TOKENS);

		if (user == null) {
			user = PublicUser.getUser(application.getDatabase());
		}

		AbstractJob job = jobService.getByIdAndUser(id, user);
		JSONObject object = JSONConverter.convert(job);

		return object.toString();

	}

	@Get("/{id}/cancel")
	@Secured(SecurityRule.IS_ANONYMOUS)
	public String cancel(@Nullable Authentication authentication, String id) {

		User user = authenticationService.getUserByAuthentication(authentication, AuthenticationType.ALL_TOKENS);

		if (user == null) {
			user = PublicUser.getUser(application.getDatabase());
		}

		AbstractJob job = jobService.getByIdAndUser(id, user);
		jobService.cancel(job);

		JSONObject object = JSONConverter.convert(job);

		return object.toString();

	}

	@Get("/{id}/restart")
	@Secured(SecurityRule.IS_ANONYMOUS)
	public HttpResponse<MessageResponse> restart(@Nullable Authentication authentication, String id) {

		User user = authenticationService.getUserByAuthentication(authentication, AuthenticationType.ALL_TOKENS);

		if (user == null) {
			user = PublicUser.getUser(application.getDatabase());
		}

		AbstractJob job = jobService.getByIdAndUser(id, user);
		jobService.restart(job);

		return HttpResponse.ok(MessageResponse.success(MESSAGE_JOB_RESTARTED));
	}

	@Get("/{id}/reset")
	@Secured(User.ROLE_ADMIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String reset(@PathVariable @NotBlank String id, @Nullable @QueryValue("max") String max) {

		int maxDownloads = 0;

		if (max != null) {
			maxDownloads = Integer.parseInt(max);
		} else {
			maxDownloads = application.getSettings().getMaxDownloads();
		}

		AbstractJob job = jobService.getById(id);

		int count = jobService.reset(job, maxDownloads);

		// TODO: use MessageResponse and update Client to support json message
		return id + ": counter of " + count + " downloads reset to " + maxDownloads;

	}

}
