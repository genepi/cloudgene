package cloudgene.mapred.api.v2.users;

import javax.validation.constraints.NotBlank;

import cloudgene.mapred.Application;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.responses.MessageResponse;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
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

	@Get("/users/activate/{user}/{code}")
	@Secured(SecurityRule.IS_ANONYMOUS)

	public HttpResponse<MessageResponse> get(@PathVariable @NotBlank String user, @PathVariable @NotBlank String code) {

		UserDao dao = new UserDao(application.getDatabase());
		User userObject = dao.findByUsername(user);

		if (userObject != null) {

			if (userObject.getActivationCode() != null && userObject.getActivationCode().equals(code)) {

				userObject.setActive(true);
				userObject.setActivationCode("");
				dao.update(userObject);
				return HttpResponse.ok(MessageResponse.success(MESSAGE_USER_ACTIVATED));

			} else {

				return HttpResponse.ok(MessageResponse.error(MESSAGE_WRONG_ACTIVATION_CODE));

			}
		} else {

			return HttpResponse.ok(MessageResponse.error(MESSAGE_WRONG_USERNAME));

		}

	}

}
