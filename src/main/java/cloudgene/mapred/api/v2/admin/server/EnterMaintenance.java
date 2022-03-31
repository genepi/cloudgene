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
public class EnterMaintenance {

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Get("/api/v2/admin/server/maintenance/enter")
	@Secured(User.ROLE_ADMIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String enterMaintenance() {

		application.getSettings().setMaintenance(true);
		application.getSettings().save();

		return "Enter Maintenance mode.";

	}

}
