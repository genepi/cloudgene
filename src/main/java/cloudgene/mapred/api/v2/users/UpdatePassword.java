package cloudgene.mapred.api.v2.users;

import cloudgene.mapred.Application;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.representations.JSONAnswer;
import cloudgene.mapred.util.HashUtil;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

@Controller
public class UpdatePassword {

	@Inject
	protected Application application;

	@Post(uri = "/api/v2/users/update-password", consumes = MediaType.APPLICATION_FORM_URLENCODED)
	@Secured(SecurityRule.IS_ANONYMOUS)
	public String post(String token, @Nullable String username, String new_password, String confirm_new_password) {

		if (username == null || username.isEmpty()) {
			return new JSONAnswer("No username set.", false).toString();
		}

		UserDao dao = new UserDao(application.getDatabase());
		User user = dao.findByUsername(username);

		if (user == null) {
			return new JSONAnswer("We couldn't find an account with that username.", false).toString();
		}

		if (!user.isActive()) {
			return new JSONAnswer("Account is not activated.", false).toString();
		}

		if (token == null || user.getActivationCode() == null || !user.getActivationCode().equals(token)) {
			return new JSONAnswer("Your recovery request is invalid or expired.", false).toString();
		}

		String error = User.checkPassword(new_password, confirm_new_password);
		if (error != null) {
			return new JSONAnswer(error, false).toString();
		}

		user.setPassword(HashUtil.hashPassword(new_password));
		user.setActivationCode("");
		dao.update(user);

		return new JSONAnswer("Password sucessfully updated.", true).toString();

	}

}
