package cloudgene.mapred.api.v2.admin;

import cloudgene.mapred.core.User;
import cloudgene.mapred.cron.CleanUpTasks;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.auth.AuthenticationService;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import jakarta.inject.Inject;

@Controller
public class RetireJobs {

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Get("/api/v2/admin/jobs/retire")
	@Secured(User.ROLE_ADMIN)	@Produces(MediaType.TEXT_PLAIN)
	public String retireJobs() {

		int notifications = CleanUpTasks.sendNotifications(application);
		int retired = CleanUpTasks.executeRetire(application.getDatabase(), application.getSettings());

		return "NotificationJob:\n" + notifications + " notifications sent." + "\n\nRetireJob:\n" + retired
				+ " jobs retired.";

	}

}
