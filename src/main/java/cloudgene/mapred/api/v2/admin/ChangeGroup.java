package cloudgene.mapred.api.v2.admin;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.JSONConverter;
import net.sf.json.JSONObject;

public class ChangeGroup extends BaseResource {

	@Post
	public Representation post(Representation entity) {

		Form form = new Form(entity);

		User user = getAuthUser();

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires user authentication.");

		}

		if (!user.isAdmin()) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires administration rights.");

		}

		String username = form.getFirstValue("username");
		if (username == null) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation("No username provided.");
		}

		String role = form.getFirstValue("role");
		if (role == null) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation("No role provided.");
		}

		UserDao dao = new UserDao(getDatabase());
		User user1 = dao.findByUsername(username);

		if (user1 == null) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation("User '" + username + "' not found.");
		}

		// update user role in database
		user1.setRoles(role.split(User.ROLE_SEPARATOR));
		dao.update(user1);

		JSONObject object = JSONConverter.convert(user1);
		return new StringRepresentation(object.toString());

	}

}
