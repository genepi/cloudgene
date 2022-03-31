package cloudgene.mapred.api.v2.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import cloudgene.mapred.apps.Application;
import cloudgene.mapred.apps.ApplicationRepository;
import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.server.auth.AuthenticationService;
import cloudgene.mapred.server.auth.AuthenticationType;
import cloudgene.mapred.server.exceptions.JsonHttpStatusException;
import cloudgene.mapred.util.PublicUser;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlApp;
import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

public class RestartJob {

	@Inject
	protected cloudgene.mapred.server.Application application;

	@Inject
	protected AuthenticationService authenticationService;

	private static final Log log = LogFactory.getLog(RestartJob.class);

	@Get("/api/v2/jobs/{id}/cancel")
	@Secured(SecurityRule.IS_ANONYMOUS)
	public String restart(@Nullable Authentication authentication, String id) {

		User user = authenticationService.getUserByAuthentication(authentication, AuthenticationType.ALL_TOKENS);

		if (user == null) {
			user = PublicUser.getUser(application.getDatabase());
		}

		if (id == null) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, "No job id specified.");
		}

		AbstractJob job = application.getWorkflowEngine().getJobById(id);

		if (job == null) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, "Job " + id + " not found.");
		}

		if (!user.isAdmin() && job.getUser().getId() != user.getId()) {
			throw new JsonHttpStatusException(HttpStatus.FORBIDDEN, "Access denied.");
		}

		Settings settings = application.getSettings();

		if (job.getState() == AbstractJob.STATE_DEAD) {

			String hdfsWorkspace = "";
			try {
				hdfsWorkspace = HdfsUtil.path(settings.getHdfsWorkspace(), id);
			} catch (NoClassDefFoundError e) {
				log.warn("Hadoop not found in classpath. Ignore HDFS Workspace.", e);
			}
			String localWorkspace = FileUtil.path(settings.getLocalWorkspace(), id);

			job.setLocalWorkspace(localWorkspace);
			job.setHdfsWorkspace(hdfsWorkspace);
			job.setSettings(settings);
			job.setRemoveHdfsWorkspace(settings.isRemoveHdfsWorkspace());

			String appId = job.getApplicationId();

			ApplicationRepository repository = settings.getApplicationRepository();
			Application application = repository.getByIdAndUser(appId, job.getUser());
			WdlApp app = null;
			try {
				app = application.getWdlApp();
			} catch (Exception e1) {

				throw new JsonHttpStatusException(HttpStatus.NOT_FOUND,
						"Application '" + appId + "' not found or the request requires user authentication.");

			}

			((CloudgeneJob) job).loadConfig(app);

			this.application.getWorkflowEngine().restart(job);

			JSONObject json = new JSONObject();
			json.put("id", id);
			json.put("message", "Your job was successfully added to the job queue.");
			json.put("success", true);
			return json.toString();
			
		} else {
			
			throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST, "Job " + id + " is not pending.");

		}
	}

}
