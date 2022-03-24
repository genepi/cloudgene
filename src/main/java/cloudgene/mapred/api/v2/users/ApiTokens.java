package cloudgene.mapred.api.v2.users;

import cloudgene.mapred.Application;
import cloudgene.mapred.core.ApiToken;
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
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

import java.security.Principal;

import org.json.JSONObject;

@Controller
public class ApiTokens {

	@Inject
	protected Application application;
	
	@Post(uri="/api/v2/users/{user}/api-token", consumes = MediaType.ALL)
	@Secured(SecurityRule.IS_AUTHENTICATED) 
	public String createApiKey(String user, @Nullable Principal principal) {

		User userObject = application.getUserByPrincipal(principal);

		if (userObject == null) {
			throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "The request requires user authentication.");
		}

		// create token
		String token = ApiToken.create(userObject, application.getSettings().getSecretKey());

		// update token
		userObject.setApiToken(token);

		UserDao userDao = new UserDao(application.getDatabase());
		boolean successful = userDao.update(userObject);

		if (successful) {

			// return token
			JSONObject answer = new JSONObject();
			answer.put("success", true);
			answer.put("message", "Creation successfull.");
			answer.put("token", token);
			answer.put("type", "plain");
			return answer.toString();

		} else {

			return new JSONAnswer("Error during API token generation.", false).toString();

		}

	}

	@Get("/api/v2/users/{user}/api-token")
	@Secured(SecurityRule.IS_AUTHENTICATED) 
	public String getApiKey(String user, @Nullable Principal principal) {

		User userObject = application.getUserByPrincipal(principal);

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
	public String revokeApiKey(String user, @Nullable Principal principal) {

		User userObject = application.getUserByPrincipal(principal);

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