package cloudgene.mapred.server.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.apps.Application;
import cloudgene.mapred.apps.ApplicationInstaller;
import cloudgene.mapred.apps.ApplicationRepository;
import cloudgene.mapred.core.Template;
import cloudgene.mapred.core.User;
import cloudgene.mapred.plugins.PluginManager;
import cloudgene.mapred.plugins.hadoop.HadoopPlugin;
import cloudgene.mapred.server.auth.AuthenticationService;
import cloudgene.mapred.server.auth.AuthenticationType;
import cloudgene.mapred.server.exceptions.JsonHttpStatusException;
import cloudgene.mapred.server.responses.ApplicationResponse;
import cloudgene.mapred.server.services.ApplicationService;
import cloudgene.mapred.util.JSONConverter;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlParameterInput;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
public class AppController {

	private static final String APPLICATION_NOT_FOUND = "Application %s not found or the request requires user authentication.";
	private static final String APPLICATION_NOT_UPDATED = "Application not updated: %s";
	private static final String APPLICATION_NOT_INSTALLED = "Application not installed. ";
	private static final String NO_URL = "No url or file location set.";
	private static final String APPLICATION_NOT_INSTALLED_NO_WORKFLOW = "Application not installed: No workflow file found.";

	@Inject
	protected cloudgene.mapred.server.Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Inject
	protected ApplicationService applicationService;

	private static final Log log = LogFactory.getLog(AppController.class);

	@Get("/api/v2/server/apps/{appId}")
	@Secured(SecurityRule.IS_ANONYMOUS)
	public String getApp(@Nullable Authentication authentication, String appId) {

		User user = authenticationService.getUserByAuthentication(authentication, AuthenticationType.ALL_TOKENS);

		Application app = applicationService.getbyIdAndUser(user, appId);

		applicationService.chcekRequirements(app);

		List<WdlApp> apps = application.getSettings().getApplicationRepository().getAllByUser(user,
				ApplicationRepository.APPS_AND_DATASETS);

		JSONObject jsonObject = JSONConverter.convert(app.getWdlApp());

		List<WdlParameterInput> params = app.getWdlApp().getWorkflow().getInputs();

		JSONArray jsonArray = JSONConverter.convert(params, apps);

		jsonObject.put("params", jsonArray);

		jsonObject.put("s3Workspace", application.getSettings().getExternalWorkspaceType().equalsIgnoreCase("S3")
				&& application.getSettings().getExternalWorkspaceLocation().isEmpty());

		String footer = this.application.getTemplate(Template.FOOTER_SUBMIT_JOB);
		if (footer != null && !footer.trim().isEmpty()) {
			jsonObject.put("footer", footer);
		}

		return jsonObject.toString();

	}

	@Delete("/api/v2/server/apps/{appId}")
	@Secured(User.ROLE_ADMIN)
	public ApplicationResponse removeApp(String appId) {
		Application app = applicationService.removeApp(appId);
		return ApplicationResponse.build(app, this.application.getSettings());
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

				ApplicationResponse appResponse = ApplicationResponse.buildWithDetails(application,
						this.application.getSettings(), repository);

				return appResponse;

			} catch (Exception e) {
				e.printStackTrace();
				throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST,
						String.format(APPLICATION_NOT_UPDATED, e.getMessage()));
			}

		} else {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, String.format(APPLICATION_NOT_FOUND, appId));
		}
	}

	@Post("/api/v2/server/apps")
	@Secured(User.ROLE_ADMIN)
	public ApplicationResponse install(@Nullable String url) {

		try {

			if (url == null) {
				throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST, NO_URL);
			}

			ApplicationRepository repository = application.getSettings().getApplicationRepository();

			try {

				Application app = repository.install(url);

				application.getSettings().save();

				if (application != null) {
					return ApplicationResponse.buildWithDetails(app, application.getSettings(), repository);
				} else {
					throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST, APPLICATION_NOT_INSTALLED_NO_WORKFLOW);
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.error(APPLICATION_NOT_INSTALLED, e);
				throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST,
						String.format(APPLICATION_NOT_INSTALLED, e.getMessage()));
			}

		} catch (Error e) {
			e.printStackTrace();
			log.error(APPLICATION_NOT_INSTALLED, e);
			throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST,
					String.format(APPLICATION_NOT_INSTALLED, e.getMessage()));
		}

	}

	@Get("/api/v2/server/apps")
	@Secured(User.ROLE_ADMIN)
	public List<ApplicationResponse> list(@Nullable @QueryValue("reload") String reload) {

		ApplicationRepository repository = application.getSettings().getApplicationRepository();

		if (reload != null && reload.equals("true")) {
			repository.reload();
		}

		List<Application> apps = new Vector<Application>(repository.getAll());
		Collections.sort(apps);

		for (Application app : apps) {
			app.checkForChanges();

		}

		return ApplicationResponse.buildWithDetails(apps, application.getSettings(), repository);

	}

}
