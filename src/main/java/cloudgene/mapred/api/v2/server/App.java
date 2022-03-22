package cloudgene.mapred.api.v2.server;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import cloudgene.mapred.apps.Application;
import cloudgene.mapred.apps.ApplicationInstaller;
import cloudgene.mapred.apps.ApplicationRepository;
import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.Environment;
import cloudgene.mapred.plugins.PluginManager;
import cloudgene.mapred.plugins.hadoop.HadoopPlugin;
import cloudgene.mapred.util.JSONConverter;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.util.Template;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlParameterInput;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
public class App {

	@Inject
	protected cloudgene.mapred.Application application;

	@Get("/api/v2/server/apps/{appId}")
	@Secured(SecurityRule.IS_ANONYMOUS)
	public String getApp(String appId, @Nullable Principal principal) {

		User user = application.getUserByPrincipal(principal);

		// TODO: check if still needed.
		try {
			appId = java.net.URLDecoder.decode(appId, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e2) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND, "Application '" + appId + "' is not in valid format.");
		}

		Settings settings = application.getSettings();

		ApplicationRepository repository = settings.getApplicationRepository();
		Application application = repository.getByIdAndUser(appId, user);

		if (application == null) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND,
					"Application '" + appId + "' not found or the request requires user authentication..");
		}

		WdlApp wdlApp = application.getWdlApp();
		if (wdlApp.getWorkflow() == null) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND, "Application '" + appId + "' is a data package.");
		}

		if (settings.isMaintenance() && (user == null || !user.isAdmin())) {
			throw new HttpStatusException(HttpStatus.SERVICE_UNAVAILABLE,
					"This functionality is currently under maintenance.");
		}

		if (wdlApp.getWorkflow().hasHdfsInputs()) {

			PluginManager manager = PluginManager.getInstance();
			if (!manager.isEnabled(HadoopPlugin.ID)) {
				throw new HttpStatusException(HttpStatus.SERVICE_UNAVAILABLE,
						"Hadoop cluster seems unreachable or misconfigured. Hadoop support is disabled, but this application requires it.");
			}
		}

		List<WdlApp> apps = repository.getAllByUser(user, false);

		JSONObject jsonObject = JSONConverter.convert(application.getWdlApp());

		List<WdlParameterInput> params = wdlApp.getWorkflow().getInputs();
		JSONArray jsonArray = JSONConverter.convert(params, apps);

		jsonObject.put("params", jsonArray);

		jsonObject.put("s3Workspace", settings.getExternalWorkspaceType().equalsIgnoreCase("S3")
				&& settings.getExternalWorkspaceLocation().isEmpty());

		String footer = this.application.getTemplate(Template.FOOTER_SUBMIT_JOB);
		if (footer != null && !footer.trim().isEmpty()) {
			jsonObject.put("footer", footer);
		}

		return jsonObject.toString();

	}

	@Delete("/api/v2/server/apps/{appId}")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public String removeApp(String appId, @Nullable Principal principal) {

		User user = application.getUserByPrincipal(principal);

		if (user == null) {
			throw new HttpStatusException(HttpStatus.FORBIDDEN, "The request requires user authentication.");
		}

		if (!user.isAdmin()) {
			throw new HttpStatusException(HttpStatus.FORBIDDEN, "The request requires administration rights.");
		}

		try {
			appId = java.net.URLDecoder.decode(appId, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e2) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND, "Application '" + appId + "' is not in valid format.");
		}

		ApplicationRepository repository = this.application.getSettings().getApplicationRepository();
		Application application = repository.getById(appId);
		if (application != null) {
			try {
				repository.remove(application);
				this.application.getSettings().save();

				JSONObject jsonObject = JSONConverter.convert(application);
				return jsonObject.toString();

			} catch (Exception e) {
				e.printStackTrace();
				throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Application not removed: " + e.getMessage());
			}
		} else {
			throw new HttpStatusException(HttpStatus.NOT_FOUND, "Application '" + appId + "' not found.");
		}
	}

	@Put(uri = "/api/v2/server/apps/{appId}", consumes = MediaType.APPLICATION_FORM_URLENCODED)
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public String updateApp(String appId, String enabled, String permission, String reinstall, String nextflowConfig,
			String nextflowProfile, String nextflowWork, @Nullable Principal principal) {

		User user = application.getUserByPrincipal(principal);

		if (user == null) {
			throw new HttpStatusException(HttpStatus.FORBIDDEN, "The request requires user authentication.");
		}

		if (!user.isAdmin()) {
			throw new HttpStatusException(HttpStatus.FORBIDDEN, "The request requires administration rights.");
		}

		try {
			appId = java.net.URLDecoder.decode(appId, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e2) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND, "Application '" + appId + "' is not in valid format.");
		}

		ApplicationRepository repository = application.getSettings().getApplicationRepository();
		Application application = repository.getById(appId);
		if (application != null) {

			try {
				// enable or disable
				if (enabled != null) {
					if (application.isEnabled() && enabled.equals("false")) {
						application.setEnabled(false);
						repository.reload();
						this.application.getSettings().save();
					} else if (!application.isEnabled() && enabled.equals("true")) {
						application.setEnabled(true);
						repository.reload();
						this.application.getSettings().save();
					}
				}

				// update permissions
				if (permission != null) {
					if (!application.getPermission().equals(permission)) {
						application.setPermission(permission);
						repository.reload();
						this.application.getSettings().save();
					}
				}

				WdlApp wdlApp = application.getWdlApp();

				if (nextflowProfile != null || nextflowConfig != null || nextflowWork != null) {

					Map<String, String> config = repository.getConfig(wdlApp);
					config.put("nextflow.config", nextflowConfig);
					config.put("nextflow.profile", nextflowProfile);
					config.put("nextflow.work", nextflowWork);
					repository.updateConfig(wdlApp, config);
				}

				// reinstall application
				if (reinstall != null) {
					if (reinstall.equals("true")) {
						boolean installed = ApplicationInstaller.isInstalled(wdlApp, this.application.getSettings());
						if (installed) {
							ApplicationInstaller.uninstall(wdlApp, this.application.getSettings());
						}
					}
				}

				application.checkForChanges();

				JSONObject jsonObject = JSONConverter.convert(application);
				updateState(application, jsonObject);

				// read config
				Map<String, String> config = repository.getConfig(wdlApp);
				jsonObject.put("config", config);

				return jsonObject.toString();

			} catch (Exception e) {
				e.printStackTrace();
				throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Application not installed: " + e.getMessage());
			}

		} else {
			throw new HttpStatusException(HttpStatus.NOT_FOUND, "Application '" + appId + "' not found.");
		}
	}

	private void updateState(Application app, JSONObject jsonObject) {
		WdlApp wdlApp = app.getWdlApp();
		if (wdlApp != null) {
			if (wdlApp.needsInstallation()) {
				boolean installed = ApplicationInstaller.isInstalled(wdlApp, application.getSettings());
				if (installed) {
					jsonObject.put("state", "completed");
				} else {
					jsonObject.put("state", "on demand");
				}
			} else {
				jsonObject.put("state", "n/a");
			}
			Map<String, String> environment = Environment.getApplicationVariables(wdlApp, application.getSettings());
			jsonObject.put("environment", environment);
		}
	}

}
