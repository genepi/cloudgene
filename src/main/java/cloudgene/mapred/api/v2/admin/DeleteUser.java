package cloudgene.mapred.api.v2.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.util.BaseResource;
import net.sf.json.JSONObject;

public class DeleteUser extends BaseResource {

	private static final Log log = LogFactory.getLog(DeleteUser.class);

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

		String username = getAttribute("username");

		// delete user from database
		UserDao dao = new UserDao(getDatabase());
		User user1 = dao.findByUsername(username);
		if (user1 != null) {
			dao.delete(user1);

			JSONObject object = JSONObject.fromObject(user1);

			log.info(String.format("Job: Deleted user %s (ID %s) (by ADMIN user ID %s - email %s)",
					user1.getUsername(), user1.getId(),
					user.getId(), user.getMail()));

			return new StringRepresentation(object.toString());
		} else {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation("User " + username + " not found.");
		}

	}

}
