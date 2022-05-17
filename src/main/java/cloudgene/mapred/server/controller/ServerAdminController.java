package cloudgene.mapred.server.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import cloudgene.mapred.core.User;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.auth.AuthenticationService;
import cloudgene.mapred.server.responses.ServerResponse;
import cloudgene.mapred.server.services.ServerService;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.oauth2.configuration.OauthClientConfigurationProperties;
import jakarta.inject.Inject;

@Controller("/api/v2/admin/server")
@Secured(User.ROLE_ADMIN)

public class ServerAdminController {

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Inject
	protected List<OauthClientConfigurationProperties> clients;

	@Inject
	protected ServerService serverService;

	@Get("/queue/block")
	@Produces(MediaType.TEXT_PLAIN)
	public String blockQueue() {
		application.getWorkflowEngine().block();
		return "Queue blocked.";
	}

	@Get("/queue/open")
	@Produces(MediaType.TEXT_PLAIN)
	public String openQueue() {
		application.getWorkflowEngine().resume();
		return "Queue opened.";
	}

	@Get("/maintenance/enter")
	@Produces(MediaType.TEXT_PLAIN)
	public String enterMaintenance() {
		application.getSettings().setMaintenance(true);
		application.getSettings().save();
		return "Enter Maintenance mode.";
	}

	@Get("/maintenance/exit")
	@Produces(MediaType.TEXT_PLAIN)
	public String exitMaintenance() {
		application.getSettings().setMaintenance(false);
		application.getSettings().save();
		return "Exit Maintenance mode.";
	}

	@Get("/cluster")
	public String getDetails() {

		return serverService.getClusterDetails();

	}

	@Get("/logs/{logfile}")
	public String getLogs(String logfile) {

		String content = serverService.tail(new File(logfile), 1000);
		return content;

	}

	@Get("/settings")
	public ServerResponse getSettings() {

		return ServerResponse.build(application.getSettings());

	}

	@Post("/settings/update")
	public ServerResponse updateSettings(String name, String backgroundColor, String foregroundColor,
			String googleAnalytics, boolean mail, String mailSmtp, String mailUser, String mailPassword,
			String mailPort, String mailName) {

		serverService.updateSettings(name, backgroundColor, foregroundColor, googleAnalytics, String.valueOf(mail),
				mailSmtp, mailPort, mailUser, mailPassword, mailName);

		return ServerResponse.build(application.getSettings());

	}

	@Get("/cloudgene-apps")
	public String list() throws ResourceException, IOException {

		ClientResource clientResource = new ClientResource("http://apps.cloudgene.io/api/apps.json");
		return clientResource.get().getText();

	}

}
