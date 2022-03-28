package cloudgene.mapred.api.v2.users;

import cloudgene.mapred.Application;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.representations.JSONAnswer;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.MailUtil;
import cloudgene.mapred.util.Template;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

@Controller
public class RegisterUser {

	public static final String DEFAULT_ROLE = "User";

	@Inject
	protected Application application;

	@Post(uri = "/api/v2/users/register", consumes = MediaType.APPLICATION_FORM_URLENCODED)
	@Secured(SecurityRule.IS_ANONYMOUS)
	public String post(String username, String full_name, String mail, String new_password,
			String confirm_new_password) {

		// check username
		String error = User.checkUsername(username);
		if (error != null) {
			return new JSONAnswer(error, false).toString();
		}
		UserDao dao = new UserDao(application.getDatabase());
		if (dao.findByUsername(username) != null) {
			return new JSONAnswer("Username already exists.", false).toString();
		}

		// check email
		error = User.checkMail(mail);
		if (error != null) {
			return new JSONAnswer(error, false).toString();
		}
		if (dao.findByMail(mail) != null) {
			return new JSONAnswer("E-Mail is already registered.", false).toString();
		}

		// check password
		error = User.checkPassword(new_password, confirm_new_password);
		if (error != null) {
			return new JSONAnswer(error, false).toString();
		}

		// check password
		error = User.checkName(full_name);
		if (error != null) {
			return new JSONAnswer(error, false).toString();
		}

		User newUser = new User();
		newUser.setUsername(username);
		newUser.setFullName(full_name);
		newUser.setMail(mail);
		newUser.setRoles(new String[] { DEFAULT_ROLE });
		newUser.setPassword(HashUtil.hashPassword(new_password));

		try {
			
			String hostname = application.getSettings().getHostname();;

			// if email server configured, send mails with activation link. Else
			// activate user immediately.

			// send email with activation code
			String activationKey = HashUtil.getActivationHash(newUser);
			newUser.setActive(false);
			newUser.setActivationCode(activationKey);
			String appName = application.getSettings().getName();
			String subject = "[" + appName + "] Signup activation";
			String activationLink = hostname + "/#!activate/" + username + "/" + activationKey;
			String body = application.getTemplate(Template.REGISTER_MAIL, full_name, application, activationLink);

			if (application.getSettings().getMail() != null) {

				activationKey = HashUtil.getActivationHash(newUser);
				newUser.setActive(false);
				newUser.setActivationCode(activationKey);

				// send email with activation code
				appName = application.getSettings().getName();
				subject = "[" + appName + "] Signup activation";
				activationLink = hostname + "/#!activate/" + username + "/" + activationKey;
				body = application.getTemplate(Template.REGISTER_MAIL, full_name, application, activationLink);

				MailUtil.send(application.getSettings(), mail, subject, body);

			} else {

				newUser.setActive(true);
				newUser.setActivationCode("");

			}

			MailUtil.notifySlack(application.getSettings(),
					"Hi! say hello to " + username + " (" + mail + ") :hugging_face:");

			dao.insert(newUser);

			return new JSONAnswer("User sucessfully created.", true).toString();

		} catch (Exception e) {

			return new JSONAnswer(e.getMessage(), false).toString();

		}

	}
}
