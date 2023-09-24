package cloudgene.mapred.api.v2.users;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import cloudgene.mapred.core.ApiToken;
import cloudgene.mapred.core.JWTUtil;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.representations.JSONAnswer;
import cloudgene.mapred.util.BaseResource;

public class ApiTokens extends BaseResource {
	private static final Log log = LogFactory.getLog(ApiTokens.class);

	public static int DEFAULT_TOKEN_LIFETIME_API_SEC = 30 * 24 * 60 * 60;

	@Post
	public Representation createApiKey(Representation entity) {
		User user = getAuthUser();

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires user authentication.");

		}

		String expirationValue = getQueryValue("expiration");

		int expiration = DEFAULT_TOKEN_LIFETIME_API_SEC;
		if (expirationValue != null) {
			expiration = Integer.parseInt(expirationValue) * 24 * 60 * 60;
		}

		// create token
		ApiToken apiToken = JWTUtil.createApiToken(user, getSettings().getSecretKey(), expiration);

		// store random hash (not access token) in database to validate token
		user.setApiToken(apiToken.getHash());
		user.setApiTokenExpiresOn(apiToken.getExpiresOn());

		UserDao userDao = new UserDao(getDatabase());
		boolean successful = userDao.update(user);

		if (successful) {

			// return token
			JSONObject answer = new JSONObject();
			answer.put("success", true);
			answer.put("message", "Creation successfull.");
			answer.put("token", apiToken.getAccessToken());
			answer.put("type", "plain");

			log.info(String.format("User: generated API token for user %s (ID %s - email %s)", user.getUsername(),
					user.getId(), user.getMail()));
			return new StringRepresentation(answer.toString());

		} else {

			return new JSONAnswer("Error during API token generation.", false);

		}

	}

	@Get
	public Representation getApiKey(Representation entity) {
		User user = getAuthUser();

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires user authentication.");

		}

		// return token
		JSONObject answer = new JSONObject();
		answer.put("success", true);
		answer.put("token", user.getApiToken());
		answer.put("type", "plain");
		return new StringRepresentation(answer.toString());

	}

	@Delete
	public Representation revokeApiKey(Representation entity) {
		User user = getAuthUser();

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires user authentication.");

		}

		// update token
		user.setApiToken("");

		UserDao userDao = new UserDao(getDatabase());
		boolean successful = userDao.update(user);

		if (successful) {

			// return token
			JSONObject answer = new JSONObject();
			answer.put("success", true);
			answer.put("message", "Creation successfull.");
			answer.put("token", "");
			answer.put("type", "plain");

			log.info(String.format("User: revoked API token for user %s (ID %s - email %s)", user.getUsername(),
					user.getId(), user.getMail()));
			return new StringRepresentation(answer.toString());

		} else {

			return new JSONAnswer("Error during API token generation.", false);

		}

	}

}
