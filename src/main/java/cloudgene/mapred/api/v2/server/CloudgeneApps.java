package cloudgene.mapred.api.v2.server;

import java.io.IOException;

import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import cloudgene.mapred.Application;
import cloudgene.mapred.auth.AuthenticationService;
import cloudgene.mapred.core.User;
import cloudgene.mapred.exceptions.JsonHttpStatusException;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

@Controller
public class CloudgeneApps {

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Get("/api/v2/server/cloudgene-apps")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public String get(Authentication authentication) throws ResourceException, IOException {

		User user = authenticationService.getUserByAuthentication(authentication);

		if (!user.isAdmin()) {
			throw new JsonHttpStatusException(HttpStatus.FORBIDDEN, "The request requires administration rights.");
		}

		// http://127.0.0.1:4000
		ClientResource clientResource = new ClientResource("http://apps.cloudgene.io/api/apps.json");
		return clientResource.get().getText();

	}

}
