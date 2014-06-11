package cloudgene.mapred.resources.users;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.representations.JSONAnswer;
import cloudgene.mapred.util.HashUtil;

public class NewUser extends ServerResource {

	@Post
	public Representation post(Representation entity) {

		UserSessions sessions = UserSessions.getInstance();
		User user = sessions.getUserByRequest(getRequest());
		if (user != null) {

			Form form = new Form(entity);

			// New User
			String username = form.getFirstValue("username");
			String role = form.getFirstValue("role-combo");
			String newPassword = form.getFirstValue("new-password");
			String confirmNewPassword = form
					.getFirstValue("confirm-new-password");

			if (username != null && !username.isEmpty()) {

				if (newPassword.equals(confirmNewPassword)) {

					User newUser = new User();
					newUser.setUsername(username);
					newUser.setRole(role);
					newUser.setPassword(HashUtil.getMD5(newPassword));

					UserDao dao = new UserDao();
					dao.insert(newUser);

					return new JSONAnswer("User sucessfully created.", true);

				} else {

					return new JSONAnswer("Please check your passwords.", false);

				}

			} else {

				return new JSONAnswer("Please enter a username.", false);

			}

		} else {

			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return new JSONAnswer("Please log in.", false);

		}

	}

}
