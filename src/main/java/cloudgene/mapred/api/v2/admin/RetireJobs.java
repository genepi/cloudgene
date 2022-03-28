package cloudgene.mapred.api.v2.admin;

import cloudgene.mapred.Application;
import cloudgene.mapred.auth.AuthenticationService;
import cloudgene.mapred.core.User;
import cloudgene.mapred.cron.CleanUpTasks;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import cloudgene.mapred.exceptions.JsonHttpStatusException;
import jakarta.inject.Inject;

@Controller
public class RetireJobs {

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Get("/api/v2/admin/jobs/retire")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	@Produces(MediaType.TEXT_PLAIN)
	public String get(Authentication authentication) {

		User user = authenticationService.getUserByAuthentication(authentication);

		if (!user.isAdmin()) {
			throw new JsonHttpStatusException(HttpStatus.UNAUTHORIZED, "The request requires administration rights.");
		}

		int notifications = CleanUpTasks.sendNotifications(application);
		int retired = CleanUpTasks.executeRetire(application.getDatabase(), application.getSettings());

		return "NotificationJob:\n" + notifications + " notifications sent." + "\n\nRetireJob:\n" + retired
				+ " jobs retired.";

	}

}
