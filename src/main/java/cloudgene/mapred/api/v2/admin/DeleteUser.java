package cloudgene.mapred.api.v2.admin;

import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.util.BaseResource;

public class DeleteUser extends BaseResource {

	@Post
	public Representation post(Representation entity) {

		Form form = new Form(entity);

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

		String id = form.getFirstValue("id");

		if (id != null) {

			// delete user from database
			UserDao dao = new UserDao(getDatabase());
			User user1 = dao.findById(Integer.parseInt(id));
			dao.delete(user1);

			JSONObject object = JSONObject.fromObject(user1);
			return new StringRepresentation(object.toString());

		} else {

			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation("User " + id + " not found.");

		}

	}

}
