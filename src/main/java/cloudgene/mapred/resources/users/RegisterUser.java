package cloudgene.mapred.resources.users;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.representations.JSONAnswer;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.MailUtil;
import cloudgene.mapred.util.Settings;

public class RegisterUser extends ServerResource {

	@Post
	public Representation post(Representation entity) {

		Form form = new Form(entity);

		// New User
		String username = form.getFirstValue("username");
		String fullname = form.getFirstValue("full-name");
		String mail = form.getFirstValue("mail").toString();
		String role = "User";
		String newPassword = form.getFirstValue("new-password");
		String confirmNewPassword = form.getFirstValue("confirm-new-password");

		// TODO: unique!!

		if (username != null && !username.isEmpty()) {

			if (newPassword.equals(confirmNewPassword)) {

				UserDao dao = new UserDao();

				if (dao.findByUsername(username) != null) {
					return new JSONAnswer("Username already exists.", false);
				}

				String key = HashUtil.getMD5(System.currentTimeMillis() + "");
				User newUser = new User();
				newUser.setUsername(username);
				newUser.setFullName(fullname);
				newUser.setMail(mail);
				newUser.setRole(role);
				newUser.setActive(false);
				newUser.setActivationCode(key);
				newUser.setPassword(HashUtil.getMD5(newPassword));

				String link = getRequest().getRootRef().toString()
						+ "/#!activate/" + username + "/" + key;

				// send email with activation code

				Settings settings = Settings.getInstance();

				try {
					MailUtil
							.send(settings.getMail().get("smtp"),
									settings.getMail().get("port"),
									settings.getMail().get("user"),
									settings.getMail().get("password"),
									settings.getMail().get("name"),
									mail,
									"[" + settings.getName()
											+ "] Signup activation",
									"Dear "
											+ fullname
											+ ",\nThis email is sent automatically by the \""
											+ settings.getName()
											+ "\" system to confirm that your profile has now been registered.\n\nTo confirm your email address, please click on this activation link "
											+ link);

					MailUtil.send(settings.getMail().get("smtp"), settings
							.getMail().get("port"),
							settings.getMail().get("user"), settings.getMail()
									.get("password"),
							settings.getMail().get("name"),
							"lukas.forer@i-med.ac.at", "[" + settings.getName()
									+ "] New user", "Username: " + username);

					dao.insert(newUser);

					return new JSONAnswer("User sucessfully created.", true);

				} catch (Exception e) {

					return new JSONAnswer(e.getMessage(), false);

				}

			} else {

				return new JSONAnswer("Please check your passwords.", false);

			}

		} else {

			return new JSONAnswer("Please enter a username.", false);

		}

	}

}
