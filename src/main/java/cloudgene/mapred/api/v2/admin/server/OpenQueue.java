package cloudgene.mapred.api.v2.admin.server;

import cloudgene.mapred.core.User;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.auth.AuthenticationService;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import jakarta.inject.Inject;

@Controller
public class OpenQueue {

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Get("/api/v2/admin/server/queue/open")
	@Secured(User.ROLE_ADMIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String openQueue() {

		application.getWorkflowEngine().resume();

		return "Queue opened.";

	}

}
