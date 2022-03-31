package cloudgene.mapred.api.v2.server;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import cloudgene.mapred.apps.Application;
import cloudgene.mapred.apps.ApplicationInstaller;
import cloudgene.mapred.apps.ApplicationRepository;
import cloudgene.mapred.core.Template;
import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.Environment;
import cloudgene.mapred.plugins.PluginManager;
import cloudgene.mapred.plugins.hadoop.HadoopPlugin;
import cloudgene.mapred.server.auth.AuthenticationService;
import cloudgene.mapred.server.auth.AuthenticationType;
import cloudgene.mapred.server.exceptions.JsonHttpStatusException;
import cloudgene.mapred.server.responses.ApplicationResponse;
import cloudgene.mapred.util.JSONConverter;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlParameterInput;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Put;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
public class App {

	@Inject
	protected cloudgene.mapred.server.Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Get("/api/v2/server/apps/{appId}")
	@Secured(SecurityRule.IS_ANONYMOUS)
	public String getApp(@Nullable Authentication authentication, String appId) {

		User user = authenticationService.getUserByAuthentication(authentication, AuthenticationType.ALL_TOKENS);

		Settings settings = application.getSettings();

		if (settings.isMaintenance() && (user == null || !user.isAdmin())) {
			throw new JsonHttpStatusException(HttpStatus.SERVICE_UNAVAILABLE,
					"This functionality is currently under maintenance.");
		}

		ApplicationRepository repository = settings.getApplicationRepository();
		Application application = repository.getByIdAndUser(appId, user);

		if (application == null) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND,
					"Application '" + appId + "' not found or the request requires user authentication..");
		}

		WdlApp wdlApp = application.getWdlApp();
		if (wdlApp.getWorkflow() == null) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, "Application '" + appId + "' is a data package.");
		}

		if (wdlApp.getWorkflow().hasHdfsInputs()) {

			PluginManager manager = PluginManager.getInstance();
			if (!manager.isEnabled(HadoopPlugin.ID)) {
				throw new JsonHttpStatusException(HttpStatus.SERVICE_UNAVAILABLE,
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
	@Secured(User.ROLE_ADMIN)
	public ApplicationResponse removeApp(String appId) {

		ApplicationRepository repository = this.application.getSettings().getApplicationRepository();
		Application application = repository.getById(appId);
		if (application != null) {
			try {
				repository.remove(application);
				this.application.getSettings().save();

				return ApplicationResponse.build(application, this.application.getSettings());

			} catch (Exception e) {
				e.printStackTrace();
				throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST, "Application not removed: " + e.getMessage());
			}
		} else {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, "Application '" + appId + "' not found.");
		}
		
	}

	@Put("/api/v2/server/apps/{appId}")
	@Secured(User.ROLE_ADMIN)
	public ApplicationResponse updateApp(String appId, @Nullable String enabled, @Nullable String permission,
			@Nullable String reinstall, @Nullable Map<String, String> config) {

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

				if (config != null) {

					Map<String, String> updatedConfig = repository.getConfig(wdlApp);
					updatedConfig.put("nextflow.config", config.get("nextflow.config"));
					updatedConfig.put("nextflow.profile", config.get("nextflow.profile"));
					updatedConfig.put("nextflow.work", config.get("nextflow.work"));
					repository.updateConfig(wdlApp, updatedConfig);
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
				ApplicationResponse appResponse = ApplicationResponse.build(application, this.application.getSettings());

				// read config
				Map<String, String> updatedConfig = repository.getConfig(wdlApp);
				appResponse.setConfigMap(updatedConfig);

				return appResponse;

			} catch (Exception e) {
				e.printStackTrace();
				throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST, "Application not updated: " + e.getMessage());
			}

		} else {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, "Application '" + appId + "' not found.");
		}
	}


}
