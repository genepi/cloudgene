package cloudgene.mapred.api.v2.users;

import cloudgene.mapred.core.Template;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.responses.MessageResponse;
import cloudgene.mapred.server.services.UserService;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.MailUtil;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

@Controller
public class RegisterUser {

	private static final String MESSAGE_USER_CREATED = "User sucessfully created.";

	private static final String MESAGE_EMAIL_ALREADY_REGISTERED = "E-Mail is already registered.";

	private static final String MESSAGE_USERNAME_ALREADY_EXISTS = "Username already exists.";

	@Inject
	protected Application application;

	@Post("/api/v2/users/register")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Secured(SecurityRule.IS_ANONYMOUS)
	public HttpResponse<MessageResponse> register(String username, String full_name, String mail, String new_password,
			String confirm_new_password) {

		// check username
		String error = User.checkUsername(username);
		if (error != null) {
			return HttpResponse.ok(MessageResponse.error(error));
		}
		UserDao dao = new UserDao(application.getDatabase());
		if (dao.findByUsername(username) != null) {
			return HttpResponse.ok(MessageResponse.error(MESSAGE_USERNAME_ALREADY_EXISTS));
		}

		// check email
		error = User.checkMail(mail);
		if (error != null) {
			return HttpResponse.ok(MessageResponse.error(error));
		}
		if (dao.findByMail(mail) != null) {
			return HttpResponse.ok(MessageResponse.error(MESAGE_EMAIL_ALREADY_REGISTERED));
		}

		// check password
		error = User.checkPassword(new_password, confirm_new_password);
		if (error != null) {
			return HttpResponse.ok(MessageResponse.error(error));
		}

		// check password
		error = User.checkName(full_name);
		if (error != null) {
			return HttpResponse.ok(MessageResponse.error(error));
		}

		User newUser = new User();
		newUser.setUsername(username);
		newUser.setFullName(full_name);
		newUser.setMail(mail);
		newUser.setRoles(new String[] { UserService.DEFAULT_ROLE });
		newUser.setPassword(HashUtil.hashPassword(new_password));

		try {

			String hostname = application.getSettings().getHostname();

			// if email server configured, send mails with activation link. Else
			// activate user immediately.

			if (application.getSettings().getMail() != null) {

				String activationKey = HashUtil.getActivationHash(newUser);
				newUser.setActive(false);
				newUser.setActivationCode(activationKey);

				// send email with activation code
				String appName = application.getSettings().getName();
				String subject = "[" + appName + "] Signup activation";
				String activationLink = hostname + "/#!activate/" + username + "/" + activationKey;
				String body = application.getTemplate(Template.REGISTER_MAIL, full_name, application, activationLink);

				MailUtil.send(application.getSettings(), mail, subject, body);

			} else {

				newUser.setActive(true);
				newUser.setActivationCode("");

			}

			MailUtil.notifySlack(application.getSettings(),
					"Hi! say hello to " + username + " (" + mail + ") :hugging_face:");

			dao.insert(newUser);

			return HttpResponse.ok(MessageResponse.success(MESSAGE_USER_CREATED));

		} catch (Exception e) {

			return HttpResponse.ok(MessageResponse.error(e.getMessage()));

		}

	}
}
