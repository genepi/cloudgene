package cloudgene.mapred.server.controller;

import cloudgene.mapred.core.ApiToken;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.auth.AuthenticationService;
import cloudgene.mapred.server.responses.ApiTokenResponse;
import cloudgene.mapred.server.responses.MessageResponse;
import cloudgene.mapred.server.responses.ValidatedApiTokenResponse;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

@Controller
public class ApiTokenController {

	private static final String MESSAGE_API_TOKEN_CREATED = "Creation successfull.";

	private static final String MESSAGE_APT_TOKEN_ERROR = "Error during API token generation.";

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	public static int DEFAULT_TOKEN_LIFETIME_API_SEC = 30 * 24 * 60 * 60;

	@Post("/api/v2/users/{username}/api-token")
	@Consumes(MediaType.ALL)
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public HttpResponse<MessageResponse> create(String username, Authentication authentication,
			@QueryValue @Nullable Integer expiration) {

		User user = authenticationService.getUserByAuthentication(authentication);
		
		if (expiration == null) {
			expiration = DEFAULT_TOKEN_LIFETIME_API_SEC;
		} else if (expiration == -1) {
			expiration = Integer.MAX_VALUE;
		} else {
			expiration = expiration * 24 * 60 * 60;
		}
		ApiToken apiToken = authenticationService.createApiToken(user, expiration);
		
		// store random hash (not access token) in database to validate token
		user.setApiToken(apiToken.getHash());
		user.setApiTokenExpiresOn(apiToken.getExpiresOn());
		
		UserDao userDao = new UserDao(application.getDatabase());
		boolean successful = userDao.update(user);

		if (successful) {

			return HttpResponse.ok(new ApiTokenResponse(apiToken));

		} else {

			return HttpResponse.ok(MessageResponse.error(MESSAGE_APT_TOKEN_ERROR));

		}

	}

	@Delete("/api/v2/users/{username}/api-token")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public HttpResponse<MessageResponse> revoke(String username, Authentication authentication) {

		User user = authenticationService.getUserByAuthentication(authentication);

		// remove token
		user.setApiToken("");
		user.setApiTokenExpiresOn(null);

		UserDao userDao = new UserDao(application.getDatabase());
		boolean successful = userDao.update(user);

		if (successful) {

			return HttpResponse.ok(MessageResponse.success(MESSAGE_API_TOKEN_CREATED));

		} else {

			return HttpResponse.ok(MessageResponse.error(MESSAGE_APT_TOKEN_ERROR));

		}

	}

	@Post("/api/v2/tokens/verify")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Secured(SecurityRule.IS_ANONYMOUS)
	public HttpResponse<ValidatedApiTokenResponse> verify(String token) {

		return HttpResponse.ok(authenticationService.validateApiToken(token));

	}

}