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

public class ResetPassword extends BaseResource {

	@Post
	public Representation get(Representation entity) {

		Form form = new Form(entity);
		String username = form.getFirstValue("username");

		if (username == null || username.isEmpty()) {
			return new JSONAnswer("Please enter a valid username or email address.", false);
		}

		UserDao dao = new UserDao(getDatabase());
		User user = dao.findByUsername(username);

		if (user == null) {
			user = dao.findByMail(username);
		}

		if (user != null) {

			if (!user.isActive()) {
				return new JSONAnswer("Account is not activated.", false);
			}
			String key = "";
			if (user.getActivationCode() != null && !user.getActivationCode().isEmpty()) {

				// resend the same activation token
				key = user.getActivationCode();
				// return new JSONAnswer("Recovery mail already sent to " +
				// user.getMail() + ". Resend again.", false);

			} else {

				// create activation token
				key = HashUtil.getMD5(System.currentTimeMillis() + "_" + Math.round(2000));
				user.setActivationCode(key);
				dao.update(user);
			}

			String link = getRequest().getRootRef().toString() + "/#!recovery/" + user.getUsername() + "/" + key;

			// send email with activation code

			String application = getSettings().getName();
			String subject = "[" + application + "] Password Recovery";
			String body = getWebApp().getTemplate(Template.RECOVERY_MAIL, user.getFullName(), application, link);

			try {

				MailUtil.notifySlack(getSettings(), "Hi! " + username + " asked for a new password :key:");

				MailUtil.send(getSettings(), user.getMail(), subject, body);

				return new JSONAnswer(
						"Email sent to " + user.getMail() + " with instructions on how to reset your password.", true);

			} catch (Exception e) {

				return new JSONAnswer("Sending recovery email failed. " + e.getMessage(), false);

			}

		} else {

			return new JSONAnswer("We couldn't find an account with that username or email.", false);

		}

	}

}
