package cloudgene.mapred.resources.users;

import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.database.UserDao;

public class DeleteUser extends ServerResource {

	@Post
	public Representation post(Representation entity) {

		Form form = new Form(entity);

		UserSessions sessions = UserSessions.getInstance();
		User user = sessions.getUserByRequest(getRequest());

		if (user != null) {

			String id = form.getFirstValue("id");

			if (id != null) {

				if (!user.isAdmin()) {
					setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
					return new StringRepresentation(
							"The request requires administration rights.");
				}

				// delete user from database
				UserDao dao = new UserDao();
				User user1 = dao.findById(Integer.parseInt(id));
				dao.delete(user1);

				JSONObject object = JSONObject.fromObject(user1);
				return new StringRepresentation(object.toString());

			} else {

				setStatus(Status.CLIENT_ERROR_NOT_FOUND);
				return new StringRepresentation("Job " + id + " not found.");

			}

		} else {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");

		}
	}

}
