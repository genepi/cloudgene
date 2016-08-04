package cloudgene.mapred.resources.users;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.JWTUtil;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.representations.JSONAnswer;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.HashUtil;

public class UpdateUserPassword extends BaseResource {

	@Post
	public Representation post(Representation entity) {

		User user = getAuthUser();

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

						UserDao dao = new UserDao(getDatabase());
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
