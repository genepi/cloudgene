package cloudgene.mapred.api.v2.users;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.representations.JSONAnswer;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.HashUtil;

public class UpdatePassword extends BaseResource {

	@Post
	public Representation post(Representation entity) {

		Form form = new Form(entity);

		// Password
		String key = form.getFirstValue("token");
		String username = form.getFirstValue("username");
		String newPassword = form.getFirstValue("new-password");
		String confirmNewPassword = form.getFirstValue("confirm-new-password");

		if (username == null || username.isEmpty()) {
			return new JSONAnswer("No username set.", false);
		}

		UserDao dao = new UserDao(getDatabase());
		User user = dao.findByUsername(username);

		if (user == null) {
			return new JSONAnswer("We couldn't find an account with that username.", false);
		}

		if (!user.isActive()){
			return new JSONAnswer("Account is not activated.", false);
		}
		
		if (key == null || user.getActivationCode() == null || !user.getActivationCode().equals(key)) {
			return new JSONAnswer("Your recovery request is invalid or expired.", false);
		}
		
		String error = User.checkPassword(newPassword, confirmNewPassword);
		if (error != null) {
			return new JSONAnswer(error, false);
		}

		user.setPassword(HashUtil.getMD5(newPassword));
		user.setActivationCode("");
		dao.update(user);

		return new JSONAnswer("Password sucessfully updated.", true);

	}

}
