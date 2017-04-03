package cloudgene.mapred.api.v2.users;

import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.JWTUtil;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.representations.JSONAnswer;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.Settings;

public class ApiTokens extends BaseResource {

	@Post
	public Representation createApiKey(Representation entity) {
		User user = getAuthUser();

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");

		}

		// create token
		String token = JWTUtil.createApiToken(user,getSettings().getSecretKey());

		// update token
		user.setApiToken(token);

		UserDao userDao = new UserDao(getDatabase());
		boolean successful = userDao.update(user);

		if (successful) {

			// return token
			JSONObject answer = new JSONObject();
			answer.put("success", true);
			answer.put("message", "Creation successfull.");
			answer.put("token", token);
			answer.put("type", "plain");
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
			return new StringRepresentation(
					"The request requires user authentication.");

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
			return new StringRepresentation(
					"The request requires user authentication.");

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
			return new StringRepresentation(answer.toString());

		} else {

			return new JSONAnswer("Error during API token generation.", false);

		}

	}

}
