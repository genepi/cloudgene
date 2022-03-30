package cloudgene.mapred.api.v2.users;

import cloudgene.mapred.Application;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.responses.MessageResponse;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.MailUtil;
import cloudgene.mapred.util.Template;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

@Controller
public class ResetPassword {

	private static final String MESSAGE_EMAIL_SENT = "Email sent to %s with instructions on how to reset your password.";

	private static final String MESSAGE_SENDING_EMAIL_FAILED = "Sending recovery email failed. ";

	private static final String MESSAGE_ACCOUNT_NOT_FOUND = "We couldn't find an account with that username or email.";

	private static final String MESSAGE_ACCOUNT_IS_INACTIVE = "Account is not activated.";

	private static final String MESSAGE_INVALID_USERNAME = "Please enter a valid username or email address.";

	@Inject
	protected Application application;

	@Post(uri = "/api/v2/users/reset", consumes = MediaType.APPLICATION_FORM_URLENCODED)
	@Secured(SecurityRule.IS_ANONYMOUS)
	public HttpResponse<MessageResponse> get(@Nullable String username) {

		if (username == null || username.isEmpty()) {
			return HttpResponse.ok(MessageResponse.error(MESSAGE_INVALID_USERNAME));
		}

		UserDao dao = new UserDao(application.getDatabase());
		User user = dao.findByUsername(username);

		if (user == null) {
			user = dao.findByMail(username);
		}

		if (user != null) {

			if (!user.isActive()) {
				return HttpResponse.ok(MessageResponse.error(MESSAGE_ACCOUNT_IS_INACTIVE));
			}

			String key = "";
			if (user.getActivationCode() != null && !user.getActivationCode().isEmpty()) {

				// resend the same activation token
				key = user.getActivationCode();

			} else {

				// create activation token
				key = HashUtil.getActivationHash(user);
				user.setActivationCode(key);
				dao.update(user);
			}

			String hostname = application.getSettings().getHostname();

			String link = hostname + "/#!recovery/" + user.getUsername() + "/" + key;

			// send email with activation code
			String app = application.getSettings().getName();
			String subject = "[" + app + "] Password Recovery";
			String body = application.getTemplate(Template.RECOVERY_MAIL, user.getFullName(), application, link);

			try {

				MailUtil.notifySlack(application.getSettings(), "Hi! " + username + " asked for a new password :key:");

				MailUtil.send(application.getSettings(), user.getMail(), subject, body);

				return HttpResponse.ok(MessageResponse.success(String.format(MESSAGE_EMAIL_SENT, user.getMail())));

			} catch (Exception e) {

				return HttpResponse.ok(MessageResponse.error(MESSAGE_SENDING_EMAIL_FAILED + e.getMessage()));

			}

		} else {

			return HttpResponse.ok(MessageResponse.error(MESSAGE_ACCOUNT_NOT_FOUND));

		}

	}

}
