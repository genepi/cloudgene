package cloudgene.mapred.api.v2.admin;

import javax.validation.constraints.NotBlank;

import cloudgene.mapred.Application;
import cloudgene.mapred.auth.AuthenticationService;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.exceptions.JsonHttpStatusException;
import cloudgene.mapred.util.JSONConverter;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import net.sf.json.JSONObject;

@Controller
public class ChangeGroup {

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Post("/api/v2/admin/users/changegroup")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public String post(Authentication authentication, @NotBlank String username, @NotBlank String role) {

		User user = authenticationService.getUserByAuthentication(authentication);

		if (!user.isAdmin()) {
			throw new JsonHttpStatusException(HttpStatus.UNAUTHORIZED, "The request requires administration rights.");
		}

		UserDao dao = new UserDao(application.getDatabase());
		User user1 = dao.findByUsername(username);

		if (user1 == null) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, "User " + username + " not found.");
		}

		// update user role in database
		user1.setRoles(role.split(User.ROLE_SEPARATOR));
		dao.update(user1);

		JSONObject object = JSONConverter.convert(user1);
		return object.toString();

	}

}
