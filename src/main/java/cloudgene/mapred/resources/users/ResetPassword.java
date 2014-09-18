package cloudgene.mapred.resources.users;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.representations.JSONAnswer;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.MailUtil;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.util.Template;

public class ResetPassword extends ServerResource {

	@Post
	public Representation get(Representation entity) {

		Form form = new Form(entity);
		String username = form.getFirstValue("username");

		if (username == null || username.isEmpty()) {
			return new JSONAnswer(
					"Please enter a valid username or email address.", false);

		}

		UserDao dao = new UserDao();
		User user = dao.findByUsername(username);

		if (user == null) {
			user = dao.findByMail(username);
		}

		if (user != null) {

			String key = HashUtil.getMD5(System.currentTimeMillis() + "");
			user.setActivationCode(key);
			dao.update(user);

			String link = getRequest().getRootRef().toString() + "/#!recovery/"
					+ user.getUsername() + "/" + key;

			// send email with activation code

			Settings settings = Settings.getInstance();
			String application = settings.getName();
			String subject = "[" + application + "] Password recocery";
			String body = settings.getTemplate(Template.RECOVERY_MAIL,
					user.getFullName(), application, link);

			try {

				MailUtil.send(settings, user.getMail(), subject, body);

				MailUtil.notifyAdmin(settings, "[" + settings.getName()
						+ "] Password Recovery", "Username: " + username);

				return new JSONAnswer("Email sent to " + user.getMail()
						+ " with instructions on how to reset your password.",
						true);

			} catch (Exception e) {

				return new JSONAnswer("Sending recovery email failed. "
						+ e.getMessage(), false);

			}

		} else {

			return new JSONAnswer(
					"We couldn't find an account with that username or email.",
					false);

		}

	}

}
