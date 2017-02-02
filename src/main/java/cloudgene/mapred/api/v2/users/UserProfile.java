package cloudgene.mapred.api.v2.users;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.representations.JSONAnswer;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.HashUtil;

public class UserProfile extends BaseResource {

	@Get
	public Representation get() {

		User user = getAuthUser();

		if (user == null) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires user authentication.");
		}

		UserDao dao = new UserDao(getDatabase());
		User updatedUser = dao.findByUsername(user.getUsername());

		JsonConfig config = new JsonConfig();
		config.setExcludes(new String[] { "password", "apiToken" });

		JSONObject object = JSONObject.fromObject(updatedUser, config);
		object.put("hasApiToken", user.getApiToken() != null && !user.getApiToken().isEmpty());

		StringRepresentation representation = new StringRepresentation(object.toString(), MediaType.APPLICATION_JSON);

		return representation;

	}

	@Post
	public Representation post(Representation entity) {

		User user = getAuthUser();

		if (user == null) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires user authentication.");
		}

		Form form = new Form(entity);

		String username = form.getFirstValue("username");
		String fullname = form.getFirstValue("full-name");
		String mail = form.getFirstValue("mail").toString();
		String newPassword = form.getFirstValue("new-password");
		String confirmNewPassword = form.getFirstValue("confirm-new-password");

		String error = User.checkUsername(username);
		if (error != null) {
			return new JSONAnswer(error, false);
		}

		// check if user is admin or it is his username
		if (!user.getUsername().equals(username) && !user.isAdmin()) {
			return new JSONAnswer("You are not allowed to change this user profile.", false);
		}

		error = User.checkName(fullname);
		if (error != null) {
			return new JSONAnswer(error, false);
		}

		error = User.checkMail(mail);
		if (error != null) {
			return new JSONAnswer(error, false);
		}

		UserDao dao = new UserDao(getDatabase());
		User newUser = dao.findByUsername(username);
		newUser.setFullName(fullname);
		newUser.setMail(mail);

		// update password only when it's not empty
		if (newPassword != null && !newPassword.isEmpty()) {

			error = User.checkPassword(newPassword, confirmNewPassword);

			if (error != null) {
				return new JSONAnswer(error, false);
			}
			newUser.setPassword(HashUtil.getMD5(newPassword));

		}

		dao.update(newUser);

		return new JSONAnswer("User profile sucessfully updated.", true);

	}

}
