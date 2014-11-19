package cloudgene.mapred.resources.users;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JsonConfig;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.util.BaseResource;

public class GetUsers extends BaseResource {

	@Get
	public Representation get() {

		User user = getUser(getRequest());

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");

		}

		if (!user.isAdmin()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires administration rights.");
		}

		UserDao dao = new UserDao(getDatabase());
		List<User> users = dao.findAll();

		JsonConfig config = new JsonConfig();
		config.setExcludes(new String[] { "password" });

		JSONArray jsonArray = JSONArray.fromObject(users, config);

		return new StringRepresentation(jsonArray.toString());

	}

}
