package cloudgene.mapred.api.v2.users;

import org.json.JSONObject;

import cloudgene.mapred.Application;
import cloudgene.mapred.auth.AuthenticationService;
import cloudgene.mapred.core.ApiToken;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.responses.ApiTokenResponse;
import cloudgene.mapred.responses.MessageResponse;
import cloudgene.mapred.responses.ValidatedApiTokenResponse;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

@Controller
public class ApiTokens {

	private static final String MESSAGE_API_TOKEN_CREATED = "Creation successfull.";

	private static final String MESSAGE_APT_TOKEN_ERROR = "Error during API token generation.";

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	public static int TOKEN_LIFETIME_API_SEC = 30 * 24 * 60 * 60;

	@Post(uri = "/api/v2/users/{username}/api-token", consumes = MediaType.ALL)
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public HttpResponse<MessageResponse> createApiKey(String username, Authentication authentication) {

		User user = authenticationService.getUserByAuthentication(authentication);

		ApiToken apiToken = authenticationService.createApiToken(user, TOKEN_LIFETIME_API_SEC);

		// store random hash (not access token) in database to validate token
		user.setApiToken(apiToken.getHash());

		UserDao userDao = new UserDao(application.getDatabase());
		boolean successful = userDao.update(user);

		if (successful) {

			return HttpResponse.ok(new ApiTokenResponse(apiToken));

		} else {

			return HttpResponse.ok(MessageResponse.error(MESSAGE_APT_TOKEN_ERROR));

		}

	}

	@Get("/api/v2/users/{username}/api-token")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public String getApiKey(String username, Authentication authentication) {

		User user = authenticationService.getUserByAuthentication(authentication);

		// TODO: remove this resource. it is unnecessary, because we never store api
		// token!
		// return token
		JSONObject answer = new JSONObject();
		answer.put("success", true);
		answer.put("token", user.getApiToken());
		answer.put("type", "plain");
		return answer.toString();

	}

	@Delete("/api/v2/users/{username}/api-token")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public HttpResponse<MessageResponse> revokeApiKey(String username, Authentication authentication) {

		User user = authenticationService.getUserByAuthentication(authentication);

		// remove token
		user.setApiToken("");

		UserDao userDao = new UserDao(application.getDatabase());
		boolean successful = userDao.update(user);

		if (successful) {

			return HttpResponse.ok(MessageResponse.success(MESSAGE_API_TOKEN_CREATED));

		} else {

			return HttpResponse.ok(MessageResponse.error(MESSAGE_APT_TOKEN_ERROR));

		}

	}

	@Post(uri = "/api/v2/tokens/verify", consumes = MediaType.APPLICATION_FORM_URLENCODED)
	@Secured(SecurityRule.IS_ANONYMOUS)
	public HttpResponse<ValidatedApiTokenResponse> verifyApiKey(String token) {

		return HttpResponse.ok(authenticationService.validateApiToken(token));

	}

}