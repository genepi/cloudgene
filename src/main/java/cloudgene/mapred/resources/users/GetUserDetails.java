package cloudgene.mapred.resources.users;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.util.BaseResource;

public class GetUserDetails extends BaseResource {

	@Get
	public Representation get() {

		User user = getAuthUser();

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");

		}

		UserDao dao = new UserDao(getDatabase());
		User updatedUser = dao.findByUsername(user.getUsername());

		JsonConfig config = new JsonConfig();
		config.setExcludes(new String[] { "password", "apiToken" });

		JSONObject object = JSONObject.fromObject(updatedUser, config);
		object.put("hasApiToken", user.getApiToken() != null
				&& !user.getApiToken().isEmpty());

		StringRepresentation representation = new StringRepresentation(
				object.toString(), MediaType.APPLICATION_JSON);

		return representation;

	}

}
