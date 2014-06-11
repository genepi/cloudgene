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

public class UpdateUserPassword extends ServerResource {

	@Post
	public Representation post(Representation entity) {
		UserSessions sessions = UserSessions.getInstance();
		User user = sessions.getUserByRequest(getRequest());
		if (user != null) {

			Form form = new Form(entity);

			// Password
			String oldPassword = form.getFirstValue("old-password");
			if (oldPassword != null && !oldPassword.isEmpty()) {

				if (HashUtil.getMD5(oldPassword).equals(user.getPassword())) {

					String newPassword = form.getFirstValue("new-password");
					String confirmNewPassword = form
							.getFirstValue("confirm-new-password");

					if (newPassword.equals(confirmNewPassword)) {

						user.setPassword(HashUtil.getMD5(newPassword));

						UserDao dao = new UserDao();
						dao.update(user);

					} else {

						return new JSONAnswer("Please check your passwords.",
								false);

					}

				} else {

					return new JSONAnswer("Wrong password.", false);

				}

			} else {

				return new JSONAnswer("Please check your passwords.", false);

			}

			return new JSONAnswer("Password sucessfully updated.", true);

		} else {

			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return new JSONAnswer("Please log in.", false);

		}

	}

}
