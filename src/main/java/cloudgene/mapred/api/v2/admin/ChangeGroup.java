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
import jakarta.inject.Inject;
import net.sf.json.JSONObject;

@Controller
public class ChangeGroup {

	private static final String MESSAGE_USER_NOT_FOUND = "User %s not found.";

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Post("/api/v2/admin/users/changegroup")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Secured(User.ROLE_ADMIN)
	public String post(Authentication authentication, @NotBlank String username, @NotBlank String role) {

		UserDao dao = new UserDao(application.getDatabase());
		User user = dao.findByUsername(username);

		if (user == null) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, String.format(MESSAGE_USER_NOT_FOUND, username));
		}

		// update user role in database
		user.setRoles(role.split(User.ROLE_SEPARATOR));
		dao.update(user);

		JSONObject object = JSONConverter.convert(user);
		return object.toString();

	}

}
