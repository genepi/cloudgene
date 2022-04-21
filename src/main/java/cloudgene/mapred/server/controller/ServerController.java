package cloudgene.mapred.server.controller;

import java.util.List;
import java.util.Vector;

import cloudgene.mapred.apps.ApplicationRepository;
import cloudgene.mapred.core.Template;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.auth.AuthenticationService;
import cloudgene.mapred.server.auth.AuthenticationType;
import cloudgene.mapred.server.responses.CounterResponse;
import cloudgene.mapred.wdl.WdlApp;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.oauth2.configuration.OauthClientConfigurationProperties;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import net.sf.json.JSONObject;

@Controller("/api/v2/server")
public class ServerController {

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Inject
	protected List<OauthClientConfigurationProperties> clients;

	@Get("/")
	@Secured(SecurityRule.IS_ANONYMOUS)
	public String get(@Nullable Authentication authentication) {

		User user = null;
		if (authentication != null) {
			user = authenticationService.getUserByAuthentication(authentication, AuthenticationType.ALL_TOKENS);
		}

		JSONObject data = new JSONObject();
		data.put("name", application.getSettings().getName());
		data.put("background", application.getSettings().getColors().get("background"));
		data.put("foreground", application.getSettings().getColors().get("foreground"));
		data.put("footer", application.getTemplate(Template.FOOTER));

		List<String> authClients = new Vector<String>();
		for (OauthClientConfigurationProperties client : clients) {
			authClients.add(client.getName());
		}
		data.put("oauth", authClients);

		if (user != null) {
			JSONObject userJson = new JSONObject();
			userJson.put("username", user.getUsername());
			userJson.put("mail", user.getMail());
			userJson.put("admin", user.isAdmin());
			userJson.put("name", user.getFullName());
			data.put("user", userJson);

			ApplicationRepository repository = application.getSettings().getApplicationRepository();
			List<WdlApp> apps = repository.getAllByUser(user, ApplicationRepository.APPS);
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
			data.put("apps", new Vector<JSONObject>());
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

	@Get("/counters")
	@Secured(SecurityRule.IS_ANONYMOUS)
	public CounterResponse counters() {

		CounterResponse response = CounterResponse.build(application.getWorkflowEngine());

		// TODO: implement a countAll method to avoid creating objects for all users!!
		// or cache number of users to avoid sql query on each load
		UserDao dao = new UserDao(application.getDatabase());
		response.setUsers(dao.findAll().size());

		return response;

	}

	@Get("/queue/block")
	@Secured(User.ROLE_ADMIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String blockQueue() {
		application.getWorkflowEngine().block();
		return "Queue blocked.";
	}
	
	@Get("/queue/open")
	@Secured(User.ROLE_ADMIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String openQueue() {
		application.getWorkflowEngine().resume();
		return "Queue opened.";
	}

	@Get("/maintenance/enter")
	@Secured(User.ROLE_ADMIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String enterMaintenance() {
		application.getSettings().setMaintenance(true);
		application.getSettings().save();
		return "Enter Maintenance mode.";
	}
	
	@Get("/maintenance/exit")
	@Secured(User.ROLE_ADMIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String exitMaintenance() {
		application.getSettings().setMaintenance(false);
		application.getSettings().save();
		return "Exit Maintenance mode.";
	}
	
	// TODO: add getStatistics

}
