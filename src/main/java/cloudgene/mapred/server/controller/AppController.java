package cloudgene.mapred.server.controller;

import java.util.List;
import java.util.Map;

import cloudgene.mapred.apps.Application;
import cloudgene.mapred.apps.ApplicationRepository;
import cloudgene.mapred.core.Template;
import cloudgene.mapred.core.User;
import cloudgene.mapred.server.auth.AuthenticationService;
import cloudgene.mapred.server.auth.AuthenticationType;
import cloudgene.mapred.server.responses.ApplicationResponse;
import cloudgene.mapred.server.responses.WdlAppResponse;
import cloudgene.mapred.server.services.ApplicationService;
import cloudgene.mapred.wdl.WdlApp;
import io.micronaut.core.annotation.Nullable;
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

@Controller
public class AppController {

	@Inject
	protected cloudgene.mapred.server.Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Inject
	protected ApplicationService applicationService;

	@Get("/api/v2/server/apps/{appId}")
	@Secured(SecurityRule.IS_ANONYMOUS)
	public WdlAppResponse getApp(@Nullable Authentication authentication, String appId) {

		User user = authenticationService.getUserByAuthentication(authentication, AuthenticationType.ALL_TOKENS);
		Application app = applicationService.getByIdAndUser(user, appId);

		applicationService.checkRequirements(app);
		ApplicationRepository repository = applicationService.getRepository();
		List<WdlApp> apps = repository.getAllByUser(user, ApplicationRepository.APPS_AND_DATASETS);

		WdlAppResponse response = WdlAppResponse.build(app.getWdlApp(), apps);

		response.setS3Workspace(application.getSettings().getExternalWorkspaceType().equalsIgnoreCase("S3")
				&& application.getSettings().getExternalWorkspaceLocation().isEmpty());

		String footer = this.application.getTemplate(Template.FOOTER_SUBMIT_JOB);
		if (footer != null && !footer.trim().isEmpty()) {
			response.setFooter(footer);
		}

		return response;

	}

	@Delete("/api/v2/server/apps/{appId}")
	@Secured(User.ROLE_ADMIN)
	public ApplicationResponse removeApp(String appId) {
		Application app = applicationService.removeApp(appId);
		return ApplicationResponse.build(app);
	}

	@Put("/api/v2/server/apps/{appId}")
	@Secured(User.ROLE_ADMIN)
	public ApplicationResponse updateApp(String appId, @Nullable Boolean enabled, @Nullable String permission,
			@Nullable Boolean reinstall, @Nullable Map<String, String> config) {

		Application app = applicationService.getById(appId);

		// enable or disable
		if (enabled != null) {
			applicationService.enableApp(app, enabled);
		}
		// update permissions
		applicationService.updatePermissions(app, permission);

		app.checkForChanges();

		ApplicationResponse appResponse = ApplicationResponse.build(app);

		return appResponse;
	}

	@Get("/api/v2/server/apps/{appId}/settings")
	@Secured(User.ROLE_ADMIN)
	public ApplicationResponse getAppSettings(String appId) {
		Application app = applicationService.getById(appId);
		ApplicationRepository repository = applicationService.getRepository();
		ApplicationResponse appResponse = ApplicationResponse.buildWithDetails(app, this.application.getSettings(),
				repository);

		return appResponse;
	}

	@Put("/api/v2/server/apps/{appId}/settings")
	@Secured(User.ROLE_ADMIN)
	public ApplicationResponse updateAppSettings(String appId, @Nullable Boolean enabled, @Nullable String permission,
			@Nullable Boolean reinstall, @Nullable Map<String, String> config) {

		Application app = applicationService.getById(appId);
		applicationService.updateConfig(app, config);

		app.checkForChanges();

		ApplicationRepository repository = applicationService.getRepository();

		ApplicationResponse appResponse = ApplicationResponse.buildWithDetails(app, this.application.getSettings(),
				repository);

		return appResponse;
	}

	@Post("/api/v2/server/apps")
	@Secured(User.ROLE_ADMIN)
	public ApplicationResponse install(@Nullable String url) {
		Application app = applicationService.installApp(url);
		return ApplicationResponse.build(app);
	}

	@Get("/api/v2/server/apps")
	@Secured(User.ROLE_ADMIN)
	public List<ApplicationResponse> list(@Nullable @QueryValue("reload") Boolean reload) {
		if (reload == null) {
			reload = false;
		}
		List<Application> apps = applicationService.listApps(reload);
		ApplicationRepository repository = applicationService.getRepository();
		return ApplicationResponse.buildWithDetails(apps, application.getSettings(), repository);
	}

}