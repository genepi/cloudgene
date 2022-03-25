package cloudgene.mapred.api.v2.server;

import java.util.List;
import java.util.Vector;

import cloudgene.mapred.Application;
import cloudgene.mapred.apps.ApplicationRepository;
import cloudgene.mapred.auth.AuthenticationService;
import cloudgene.mapred.auth.AuthenticationType;
import cloudgene.mapred.core.User;
import cloudgene.mapred.util.Template;
import cloudgene.mapred.wdl.WdlApp;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import net.sf.json.JSONObject;

@Controller
public class Server {

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;
	
	@Get("/api/v2/server")
	@Secured(SecurityRule.IS_ANONYMOUS)
	public String getServer(@Nullable Authentication authentication) {

		User user = authenticationService.getUserByAuthentication(authentication, AuthenticationType.ALL_TOKENS);

		JSONObject data = new JSONObject();
		data.put("name", application.getSettings().getName());
		data.put("background", application.getSettings().getColors().get("background"));
		data.put("foreground", application.getSettings().getColors().get("foreground"));
		data.put("footer", application.getTemplate(Template.FOOTER));

		if (user != null) {
			JSONObject userJson = new JSONObject();
			userJson.put("username", user.getUsername());
			userJson.put("mail", user.getMail());
			userJson.put("admin", user.isAdmin());
			userJson.put("name", user.getFullName());
			data.put("user", userJson);

			ApplicationRepository repository = application.getSettings().getApplicationRepository();
			List<WdlApp> apps = repository.getAllByUser(user);
			data.put("apps", apps);

			List<JSONObject> appsJson = new Vector<JSONObject>();
			List<JSONObject> deprecatedAppsJson = new Vector<JSONObject>();
			List<JSONObject> experimentalAppsJson = new Vector<JSONObject>();

			for (WdlApp app : apps) {
				JSONObject appJson = new JSONObject();
				appJson.put("id", app.getId());
				appJson.put("name", app.getName());
				if (app.getRelease() == null) {
					appsJson.add(appJson);
				} else if (app.getRelease().equals("deprecated")) {
					deprecatedAppsJson.add(appJson);
				} else if (app.getRelease().equals("experimental")) {
					experimentalAppsJson.add(appJson);
				} else {
					appsJson.add(appJson);
				}
			}

			data.put("apps", appsJson);
			data.put("deprecatedApps", deprecatedAppsJson);
			data.put("experimentalApps", experimentalAppsJson);
			data.put("loggedIn", true);

		} else {
			// get Public apps
			ApplicationRepository repository = application.getSettings().getApplicationRepository();
			List<WdlApp> apps = repository.getAllByUser(null);
			data.put("apps", apps);
			data.put("loggedIn", false);
		}

		data.put("navigation", application.getSettings().getNavigation());
		if (application.getSettings().isMaintenance()) {
			data.put("maintenace", true);
			data.put("maintenaceMessage", application.getTemplate(Template.MAINTENANCE_MESSAGE));
		} else {
			data.put("maintenace", false);
		}

		return data.toString();

	}

}
