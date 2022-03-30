package cloudgene.mapred.api.v2.users;

import cloudgene.mapred.Application;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.responses.MessageResponse;
import cloudgene.mapred.util.HashUtil;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

@Controller
public class UpdatePassword {

	private static final String MESSAGE_NO_USERNAME_SET = "No username set.";

	private static final String MESSAGE_PASSWORD_UPDATED = "Password sucessfully updated.";

	private static final String MESSAGE_INVALID_RECOVERY_REQUEST = "Your recovery request is invalid or expired.";

	private static final String MESSAGE_ACCOUNT_IS_INACTIVE = "Account is not activated.";

	private static final String MESSAGE_ACCOUNT_NOT_FOUND = "We couldn't find an account with that username.";

	@Inject
	protected Application application;

	@Post(uri = "/api/v2/users/update-password", consumes = MediaType.APPLICATION_FORM_URLENCODED)
	@Secured(SecurityRule.IS_ANONYMOUS)
	public HttpResponse<MessageResponse> post(String token, @Nullable String username, String new_password,
			String confirm_new_password) {

		if (username == null || username.isEmpty()) {
			return HttpResponse.ok(MessageResponse.error(MESSAGE_NO_USERNAME_SET));
		}

		UserDao dao = new UserDao(application.getDatabase());
		User user = dao.findByUsername(username);

		if (user == null) {
			return HttpResponse.ok(MessageResponse.error(MESSAGE_ACCOUNT_NOT_FOUND));
		}

		if (!user.isActive()) {
			return HttpResponse.ok(MessageResponse.error(MESSAGE_ACCOUNT_IS_INACTIVE));
		}

		if (token == null || user.getActivationCode() == null || !user.getActivationCode().equals(token)) {
			return HttpResponse.ok(MessageResponse.error(MESSAGE_INVALID_RECOVERY_REQUEST));
		}

		String error = User.checkPassword(new_password, confirm_new_password);
		if (error != null) {
			return HttpResponse.ok(MessageResponse.error(error));
		}

		user.setPassword(HashUtil.hashPassword(new_password));
		user.setActivationCode("");
		dao.update(user);

		return HttpResponse.ok(MessageResponse.success(MESSAGE_PASSWORD_UPDATED));

	}

}
