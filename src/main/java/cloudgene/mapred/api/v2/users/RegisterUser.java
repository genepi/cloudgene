package cloudgene.mapred.api.v2.users;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.representations.JSONAnswer;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.MailUtil;
import cloudgene.mapred.util.Template;

public class RegisterUser extends BaseResource {

	public static final String DEFAULT_ROLE = "User";

	@Post
	public Representation post(Representation entity) {

		Form form = new Form(entity);
		String username = form.getFirstValue("username");
		String fullname = form.getFirstValue("full-name");
		String mail = form.getFirstValue("mail").toString();
		String newPassword = form.getFirstValue("new-password");
		String confirmNewPassword = form.getFirstValue("confirm-new-password");

		// check username
		String error = User.checkUsername(username);
		if (error != null) {
			return new JSONAnswer(error, false);
		}
		UserDao dao = new UserDao(getDatabase());
		if (dao.findByUsername(username) != null) {
			return new JSONAnswer("Username already exists.", false);
		}

		// check email
		error = User.checkMail(mail);
		if (error != null) {
			return new JSONAnswer(error, false);
		}
		if (dao.findByMail(mail) != null) {
			return new JSONAnswer("E-Mail is already registered.", false);
		}

		// check password
		error = User.checkPassword(newPassword, confirmNewPassword);
		if (error != null) {
			return new JSONAnswer(error, false);
		}

		// check password
		error = User.checkName(fullname);
		if (error != null) {
			return new JSONAnswer(error, false);
		}

		User newUser = new User();
		newUser.setUsername(username);
		newUser.setFullName(fullname);
		newUser.setMail(mail);
		newUser.setRoles(new String[] { DEFAULT_ROLE });
		newUser.setPassword(HashUtil.getMD5(newPassword));

		try {

			// if email server configured, send mails with activation link. Else
			// activate user immediately.

			if (getSettings().getMail() != null) {

				String activationKey = HashUtil.getMD5(System.currentTimeMillis() + username + mail);
				newUser.setActive(false);
				newUser.setActivationCode(activationKey);

				// send email with activation code
				String application = getSettings().getName();
				String subject = "[" + application + "] Signup activation";
				String activationLink = getRequest().getRootRef().toString() + "/#!activate/" + username + "/"
						+ activationKey;
				String body = getWebApp().getTemplate(Template.REGISTER_MAIL, fullname, application, activationLink);

				MailUtil.send(getSettings(), mail, subject, body);

			} else {

				newUser.setActive(true);
				newUser.setActivationCode("");

			}

			MailUtil.notifySlack(getSettings(), "Hi! say hello to " + username + " (" + mail + ") :hugging_face:");

			dao.insert(newUser);

			return new JSONAnswer("User sucessfully created.", true);

		} catch (Exception e) {

			return new JSONAnswer(e.getMessage(), false);

		}

	}
}
