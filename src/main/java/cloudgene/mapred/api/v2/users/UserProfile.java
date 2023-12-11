package cloudgene.mapred.api.v2.users;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.representations.JSONAnswer;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.JSONConverter;
import net.sf.json.JSONObject;

public class UserProfile extends BaseResource {
	private static final Log log = LogFactory.getLog(UserProfile.class);

	@Get
	public Representation get() {

		User user = getAuthUserAndAllowApiToken();

		if (user == null) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires user authentication.");
		}

		UserDao dao = new UserDao(getDatabase());
		User updatedUser = dao.findByUsername(user.getUsername());

		JSONObject object = JSONConverter.convert(updatedUser);

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
		String mail = form.getFirstValue("mail");
		String newPassword = form.getFirstValue("new-password");
		String confirmNewPassword = form.getFirstValue("confirm-new-password");

		String error = User.checkUsername(username);
		if (error != null) {
			return new JSONAnswer(error, false);
		}

		// check if user is admin or it is his username
		if (!user.getUsername().equals(username) && !user.isAdmin()) {
			log.error(String.format("User: ID %s ('%s') attempted to change profile of a different user '%s'",
					user.getId(), user.getUsername(), username));
			return new JSONAnswer("You are not allowed to change this user profile.", false);
		}

		error = User.checkName(fullname);
		if (error != null) {
			return new JSONAnswer(error, false);
		}

		boolean mailProvided = (mail != null && !mail.isEmpty());

		if (getSettings().isEmailRequired() || mailProvided) {
			error = User.checkMail(mail);
			if (error != null) {
				return new JSONAnswer(error, false);
			}
		}

		UserDao dao = new UserDao(getDatabase());
		User newUser = dao.findByUsername(username);
		newUser.setFullName(fullname);
		newUser.setMail(mail);

		if (user.getMail() != null && !user.getMail().equals(newUser.getMail())) {
			log.info(String.format("User: changed email address for user %s (ID %s)", newUser.getUsername(),
					newUser.getId()));
		}

		String roleMessage = " ";
		if (!getSettings().isEmailRequired()) {
			if ((newUser.getMail() == null || newUser.getMail().isEmpty()) && user.hasRole(RegisterUser.DEFAULT_ROLE)) {
				newUser.replaceRole(RegisterUser.DEFAULT_ROLE, RegisterUser.DEFAULT_ANONYMOUS_ROLE);
				log.info(String.format("User: changed role to %s for user %s (ID %s)", RegisterUser.DEFAULT_ANONYMOUS_ROLE, newUser.getUsername(),
						newUser.getId()));
				roleMessage += "<br><br>Your account has been <b>downgraded</b>.<br>To apply these changes, please log out and log back in.";
			} else if ((newUser.getMail() != null && !newUser.getMail().isEmpty()) && user.hasRole(RegisterUser.DEFAULT_ANONYMOUS_ROLE)) {
				newUser.replaceRole(RegisterUser.DEFAULT_ANONYMOUS_ROLE, RegisterUser.DEFAULT_ROLE);
				log.info(String.format("User: changed role to %s for user %s (ID %s)", RegisterUser.DEFAULT_ROLE, newUser.getUsername(),
						newUser.getId()));
				roleMessage += "<br><br>Your account has been <b>upgraded</b>.<br>To apply these changes, please log out and log back in.";
			}
		}

		// update password only when it's not empty
		if (newPassword != null && !newPassword.isEmpty()) {

			error = User.checkPassword(newPassword, confirmNewPassword);

			if (error != null) {
				return new JSONAnswer(error, false);
			}
			newUser.setPassword(HashUtil.hashPassword(newPassword));

			log.info(String.format("User: changed password for user %s (ID %s - email %s)", newUser.getUsername(),
					newUser.getId(), newUser.getMail()));
		}

		dao.update(newUser);

		return new JSONAnswer("User profile successfully updated." + roleMessage , true);

	}

	@Delete
	public Representation delete(Representation entity) {

		User user = getAuthUser();

		if (user == null) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return error401("The request requires user authentication.");
		}

		Form form = new Form(entity);
		String username = form.getFirstValue("username");
		String password = form.getFirstValue("password");

		// check if user is admin or it is his username
		if (!user.getUsername().equals(username)) {
			return error401("You are not allowed to delete this user profile.");
		}

		if (HashUtil.checkPassword(password, user.getPassword())) {

			UserDao dao = new UserDao(getDatabase());
			log.info(String.format("User: requested deletion of account %s (ID %s - email %s)", user.getUsername(),
					user.getId(), user.getMail()));
			boolean deleted = dao.delete(user);
			if (deleted) {
				return new JSONAnswer("User profile sucessfully delete.", true);
			} else {
				return error400("Error during deleting your user profile.");
			}

		} else {
			return error401("Wrong password.");
		}

	}

}
