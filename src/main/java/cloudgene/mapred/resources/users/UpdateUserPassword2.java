package cloudgene.mapred.resources.users;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.representations.JSONAnswer;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.HashUtil;

public class UpdateUserPassword2 extends BaseResource {

	@Post
	public Representation post(Representation entity) {

		Form form = new Form(entity);

		// Password
		String key = form.getFirstValue("token");
		String username = form.getFirstValue("username");
		String newPassword = form.getFirstValue("new-password");
		String confirmNewPassword = form.getFirstValue("confirm-new-password");

		UserDao dao = new UserDao(getDatabase());
		User user = dao.findByUsername(username);

		if (user == null) {
			return new JSONAnswer(
					"We couldn't find an account with that username.", false);

		}

		System.out.println(user.getActivationCode());

		if (key == null || user.getActivationCode() == null
				|| !user.getActivationCode().equals(key)) {
			return new JSONAnswer(
					"Your recovery request is invalid or expired.", false);

		}

		if (!newPassword.equals(confirmNewPassword)) {

			return new JSONAnswer("Please check your passwords.", false);

		}

		user.setPassword(HashUtil.getMD5(newPassword));
		user.setActivationCode("");
		dao.update(user);
		
		UserSessions sessions = getUserSessions();
		sessions.updateUser(user);

		return new JSONAnswer("Password sucessfully updated.", true);

	}

}
