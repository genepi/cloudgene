package cloudgene.mapred.api.v2.users;

import javax.validation.constraints.NotBlank;

import cloudgene.mapred.Application;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.representations.JSONAnswer;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

@Controller
public class ActivateUser {

	@Inject
	protected Application application;

	@Get("/users/activate/{user}/{code}")
	@Secured(SecurityRule.IS_ANONYMOUS) 
	
	public String get(@PathVariable @NotBlank String user, @PathVariable @NotBlank String code) {

		UserDao dao = new UserDao(application.getDatabase());
		User userObject = dao.findByUsername(user);

		if (userObject != null) {
			
			if (userObject.getActivationCode() != null && userObject.getActivationCode().equals(code)) {

				userObject.setActive(true);
				userObject.setActivationCode("");
				dao.update(userObject);
				return new JSONAnswer("User sucessfully activated.", true).toString();

			} else {

				return new JSONAnswer("Wrong activation code.", false).toString();

			}
		} else {

			return new JSONAnswer("Wrong username.", false).toString();

		}

	}

}
