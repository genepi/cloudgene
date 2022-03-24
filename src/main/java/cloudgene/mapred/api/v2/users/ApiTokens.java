package cloudgene.mapred.api.v2.users;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang.RandomStringUtils;
import org.json.JSONObject;

import cloudgene.mapred.Application;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.representations.JSONAnswer;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.security.token.jwt.generator.JwtTokenGenerator;
import jakarta.inject.Inject;

@Controller
public class ApiTokens {

	@Inject
	protected Application application;

	@Inject
	protected JwtTokenGenerator tokenGenerator;

	public static int TOKEN_LIFETIME_API_SEC = 30 * 24 * 60 * 60;

	@Post(uri = "/api/v2/users/{username}/api-token", consumes = MediaType.ALL)
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public String createApiKey(String username, @Nullable Authentication authentication) {

		User user = application.getUserByAuthentication(authentication);

		if (user == null) {
			throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "The request requires user authentication.");
		}

		// create token

		String apiHash = RandomStringUtils.random(30);

		Map<String, Object> attribtues = new HashMap<String, Object>();
		attribtues.put("token_type", "API");
		attribtues.put("api_hash", apiHash);

		Authentication authentication2 = Authentication.build(user.getUsername(), attribtues);
		Optional<String> token = tokenGenerator.generateToken(authentication2, TOKEN_LIFETIME_API_SEC);

		// update token
		user.setApiToken(apiHash);

		UserDao userDao = new UserDao(application.getDatabase());
		boolean successful = userDao.update(user);

		if (successful) {

			// return token
			JSONObject answer = new JSONObject();
			answer.put("success", true);
			answer.put("message", "Creation successfull.");
			answer.put("token", token.get());
			answer.put("type", "plain");
			return answer.toString();

		} else {

			return new JSONAnswer("Error during API token generation.", false).toString();

		}

	}

	@Get("/api/v2/users/{user}/api-token")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public String getApiKey(String user, @Nullable Authentication authentication) {

		User userObject = application.getUserByAuthentication(authentication);

		if (userObject == null) {
			throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "The request requires user authentication.");
		}

		// return token
		JSONObject answer = new JSONObject();
		answer.put("success", true);
		answer.put("token", userObject.getApiToken());
		answer.put("type", "plain");
		return answer.toString();

	}

	@Delete("/api/v2/users/{user}/api-token")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public String revokeApiKey(String user, @Nullable Authentication authentication) {

		User userObject = application.getUserByAuthentication(authentication);

		if (userObject == null) {
			throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "The request requires user authentication.");
		}

		// update token
		userObject.setApiToken("");

		UserDao userDao = new UserDao(application.getDatabase());
		boolean successful = userDao.update(userObject);

		if (successful) {

			// return token
			JSONObject answer = new JSONObject();
			answer.put("success", true);
			answer.put("message", "Creation successfull.");
			answer.put("token", "");
			answer.put("type", "plain");
			return answer.toString();

		} else {

			return new JSONAnswer("Error during API token generation.", false).toString();

		}

	}

}