package cloudgene.mapred.resources.users;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.representations.JSONAnswer;
import cloudgene.mapred.util.HashUtil;

public class UpdateUser2 extends ServerResource {

	@Post
	public Representation post(Representation entity) {

		UserSessions sessions = UserSessions.getInstance();
		User user = sessions.getUserByRequest(getRequest());
		if (user != null) {

			Form form = new Form(entity);

			String username = form.getFirstValue("username");
			String fullname = form.getFirstValue("full-name");
			String mail = form.getFirstValue("mail").toString();

			String newPassword = form.getFirstValue("new-password");
			String confirmNewPassword = form
					.getFirstValue("confirm-new-password");

			if (username != null && !username.isEmpty()
					&& user.getUsername().equals(username)) {

				UserDao dao = new UserDao();
				User newUser = dao.findByUsername(username);
				newUser.setFullName(fullname);
				newUser.setMail(mail);

				if (newPassword != null && !newPassword.isEmpty()) {

					if (newPassword.equals(confirmNewPassword)) {

						newUser.setPassword(HashUtil.getMD5(newPassword));

					} else {

						return new JSONAnswer("Please check your passwords.",
								false);

					}

				}

				dao.update(newUser);

			} else {

				return new JSONAnswer("Please enter a username.", false);

			}

			return new JSONAnswer("User sucessfully updated.", true);

		} else {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");

		}

	}

}
