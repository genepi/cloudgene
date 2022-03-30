package cloudgene.mapred.api.v2.users;

import cloudgene.mapred.Application;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.responses.MessageResponse;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

@Controller
public class ActivateUser {

	private static final String MESSAGE_WRONG_USERNAME = "Wrong username.";

	private static final String MESSAGE_WRONG_ACTIVATION_CODE = "Wrong activation code.";

	private static final String MESSAGE_USER_ACTIVATED = "User sucessfully activated.";

	@Inject
	protected Application application;

	@Get("/users/activate/{username}/{code}")
	@Secured(SecurityRule.IS_ANONYMOUS)
	public HttpResponse<MessageResponse> activate(String username, String code) {

		UserDao dao = new UserDao(application.getDatabase());
		User user = dao.findByUsername(username);

		if (user != null) {

			if (user.getActivationCode() != null && user.getActivationCode().equals(code)) {

				user.setActive(true);
				user.setActivationCode("");
				dao.update(user);

				return HttpResponse.ok(MessageResponse.success(MESSAGE_USER_ACTIVATED));

			} else {

				return HttpResponse.ok(MessageResponse.error(MESSAGE_WRONG_ACTIVATION_CODE));

			}

		} else {

			return HttpResponse.ok(MessageResponse.error(MESSAGE_WRONG_USERNAME));

		}

	}

}
