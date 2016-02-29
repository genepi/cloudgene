package cloudgene.mapred.resources.users;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.representations.JSONAnswer;
import cloudgene.mapred.util.BaseResource;

public class UpdateUserSettings extends BaseResource {

	@Post
	public Representation post(Representation entity) {

		User user = getAuthUser();

		if (user != null) {
			try {
				JsonRepresentation represent = new JsonRepresentation(entity);
				JSONObject obj = represent.getJsonObject();

				// General Informations
				user.setFullName(obj.get("full-name").toString());
				user.setMail(obj.get("mail").toString());

				UserDao dao = new UserDao(getDatabase());
				dao.update(user);

				UserSessions sessions = getUserSessions();
				sessions.updateUser(user);

				return new JSONAnswer("Password sucessfully updated.", true);

			} catch (JSONException e) {

				getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				e.printStackTrace();
				return new JSONAnswer("Please log in.", false);

			} catch (IOException e) {

				getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);

				e.printStackTrace();
				return new JSONAnswer("Please log in.", false);
			}

		} else {

			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return new JSONAnswer("Please log in.", false);

		}

	}
}
