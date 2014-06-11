package cloudgene.mapred.resources.users;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.database.UserDao;

public class GetUserDetails extends ServerResource {

	@Get
	public Representation get() {

		UserSessions sessions = UserSessions.getInstance();
		User user = sessions.getUserByRequest(getRequest());

		if (user != null) {

			UserDao dao = new UserDao();
			User updatedUser = dao.findByUsername(user.getUsername());

			JsonConfig config = new JsonConfig();
			config.setExcludes(new String[] { "password" });

			JSONObject object = JSONObject.fromObject(updatedUser, config);

			StringRepresentation representation = new StringRepresentation(
					object.toString(), MediaType.APPLICATION_JSON);

			return representation;

		} else {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED );
			return new StringRepresentation("The request requires user authentication.");

		}
	}

}
