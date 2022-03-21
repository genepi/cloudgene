package cloudgene.mapred.api.v2.users;

import cloudgene.mapred.Application;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.representations.JSONAnswer;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.MailUtil;
import cloudgene.mapred.util.Template;
import io.micronaut.context.env.Environment;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

@Controller
public class ResetPassword {

	protected final String hostname;

	public ResetPassword(EmbeddedServer embeddedServer) {
		hostname = embeddedServer.getURL().toString();
	}

	@Inject
	protected Application application;

	@Post(uri = "/api/v2/users/reset", consumes = MediaType.APPLICATION_FORM_URLENCODED)
	@Secured(SecurityRule.IS_ANONYMOUS)
	public String get(String username) {

		if (username == null || username.isEmpty()) {
			return new JSONAnswer("Please enter a valid username or email address.", false).toString();
		}

		UserDao dao = new UserDao(application.getDatabase());
		User user = dao.findByUsername(username);

		if (user == null) {
			user = dao.findByMail(username);
		}

		if (user != null) {

			if (!user.isActive()) {
				return new JSONAnswer("Account is not activated.", false).toString();
			}
			String key = "";
			if (user.getActivationCode() != null && !user.getActivationCode().isEmpty()) {

				// resend the same activation token
				key = user.getActivationCode();
				// return new JSONAnswer("Recovery mail already sent to " +
				// user.getMail() + ". Resend again.", false);

			} else {

				// create activation token
				key = HashUtil.getActivationHash(user);
				user.setActivationCode(key);
				dao.update(user);
			}

			String link = hostname + "/#!recovery/" + user.getUsername() + "/" + key;

			System.out.println("LLLL " + link);

			// send email with activation code

			String app = application.getSettings().getName();
			String subject = "[" + app + "] Password Recovery";
			String body = application.getTemplate(Template.RECOVERY_MAIL, user.getFullName(), application, link);

			try {

				MailUtil.notifySlack(application.getSettings(), "Hi! " + username + " asked for a new password :key:");

				MailUtil.send(application.getSettings(), user.getMail(), subject, body);

				return new JSONAnswer(
						"Email sent to " + user.getMail() + " with instructions on how to reset your password.", true)
								.toString();

			} catch (Exception e) {

				return new JSONAnswer("Sending recovery email failed. " + e.getMessage(), false).toString();

			}

		} else {

			return new JSONAnswer("We couldn't find an account with that username or email.", false).toString();

		}

	}

}
