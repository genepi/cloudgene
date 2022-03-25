package cloudgene.mapred.api.v2.jobs;

import java.util.List;
import java.util.Vector;

import javax.validation.constraints.NotBlank;

import cloudgene.mapred.Application;
import cloudgene.mapred.auth.AuthenticationService;
import cloudgene.mapred.auth.AuthenticationType;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneParameterOutput;
import cloudgene.mapred.jobs.workspace.ExternalWorkspaceFactory;
import cloudgene.mapred.util.JSONConverter;
import cloudgene.mapred.util.PublicUser;
import cloudgene.mapred.util.Settings;
import cloudgene.sdk.internal.IExternalWorkspace;
import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import net.sf.json.JSONObject;

@Controller
public class GetJobDetails {

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;
	
	@Get("/api/v2/jobs/{id}")
	@Secured(SecurityRule.IS_ANONYMOUS) 
	public String getJob(@PathVariable @NotBlank String id, @Nullable Authentication authentication) {

		User user = authenticationService.getUserByAuthentication(authentication, AuthenticationType.ALL_TOKENS);

		if (user == null) {
			user = PublicUser.getUser(application.getDatabase());
		}

		if (application.getSettings().isMaintenance() && !user.isAdmin()) {
			throw new HttpStatusException(HttpStatus.SERVICE_UNAVAILABLE,
					"This functionality is currently under maintenance.");
		}

		// running job is in workflow engine
		AbstractJob job = application.getWorkflowEngine().getJobById(id);

		if (job == null) {
			// finished job is in database
			JobDao dao = new JobDao(application.getDatabase());
			job = dao.findById(id, true);

		}

		if (job == null) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND, "Job " + id + " not found.");
		}

		// check permissions
		if (!user.isAdmin() && (job.getUser().getId() != user.getId())) {
			throw new HttpStatusException(HttpStatus.FORBIDDEN, "Access denied.");
		}

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

	@Delete("/api/v2/jobs/{id}")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public String deleteJob(@PathVariable @NotBlank String id, Authentication authentication) {

		User user = authenticationService.getUserByAuthentication(authentication);

		if (user == null) {
			throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "The request requires user authentication.");
		}

		if (application.getSettings().isMaintenance() && !user.isAdmin()) {
			throw new HttpStatusException(HttpStatus.SERVICE_UNAVAILABLE,
					"This functionality is currently under maintenance.");
		}

		// delete job from database
		JobDao dao = new JobDao(application.getDatabase());
		AbstractJob job = dao.findById(id);

		if (job == null) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND, "Job " + id + " not found.");
		}

		// check permissions
		if (!user.isAdmin() && (job.getUser().getId() != user.getId())) {
			throw new HttpStatusException(HttpStatus.FORBIDDEN, "Access denied.");
		}

		Settings settings = application.getSettings();

		// delete local directory and hdfs directory
		String localOutput = FileUtil.path(settings.getLocalWorkspace(), job.getId());

		FileUtil.deleteDirectory(localOutput);

		try {
			String hdfsOutput = HdfsUtil.makeAbsolute(HdfsUtil.path(settings.getHdfsWorkspace(), job.getId()));
			HdfsUtil.delete(hdfsOutput);
		} catch (NoClassDefFoundError e) {
			// TODO: handle exception
		}

		// delete job from database
		job.setState(AbstractJob.STATE_DELETED);
		dao.update(job);

		IExternalWorkspace externalWorkspace = null;
		if (!settings.getExternalWorkspaceLocation().isEmpty()) {
			String externalOutput = settings.getExternalWorkspaceLocation();
			externalWorkspace = ExternalWorkspaceFactory.get(settings.getExternalWorkspaceType(), externalOutput);
			try {
				externalWorkspace.delete(job.getId());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		JSONObject object = JSONConverter.convert(job);

		return object.toString();

	}

}
