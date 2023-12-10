package cloudgene.mapred.server.controller;

import java.util.List;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.auth.AuthenticationService;
import cloudgene.mapred.server.auth.AuthenticationType;
import cloudgene.mapred.server.responses.CounterResponse;
import cloudgene.mapred.server.services.ServerService;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.oauth2.configuration.OauthClientConfigurationProperties;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

@Controller("/api/v2/server")
public class ServerController {

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Inject
	protected List<OauthClientConfigurationProperties> clients;
	
	@Inject
	protected ServerService serverService;

	@Get("/")
	@Secured(SecurityRule.IS_ANONYMOUS)
	public String get(@Nullable Authentication authentication) {
		User user = null;
		if (authentication != null) {
			user = authenticationService.getUserByAuthentication(authentication, AuthenticationType.ALL_TOKENS);
		}
		
		return serverService.getRoot(user);

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
	
	@Get("/version.svg")
	public HttpResponse<String> getVersion() {
		return HttpResponse.ok(ServerService.IMAGE_DATA);

	}
	
}
