package cloudgene.mapred.api.v2.admin;

import java.util.List;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.JSONConverter;
import net.sf.json.JSONArray;

public class GetUsers extends BaseResource {

	@Get
	public Representation get() {

		User user = getAuthUser();

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

		JSONArray jsonArray = JSONConverter.convertUsers(users);
		
		return new StringRepresentation(jsonArray.toString());

	}

}
