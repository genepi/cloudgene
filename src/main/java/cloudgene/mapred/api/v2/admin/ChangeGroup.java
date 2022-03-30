package cloudgene.mapred.api.v2.admin;

import javax.validation.constraints.NotBlank;

import cloudgene.mapred.Application;
import cloudgene.mapred.auth.AuthenticationService;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.exceptions.JsonHttpStatusException;
import cloudgene.mapred.responses.UserResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import jakarta.inject.Inject;

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
	public UserResponse post(@NotBlank String username, @NotBlank String role) {

		UserDao dao = new UserDao(application.getDatabase());
		User user = dao.findByUsername(username);

		if (user == null) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, String.format(MESSAGE_USER_NOT_FOUND, username));
		}

		// update user role in database
		user.setRoles(role.split(User.ROLE_SEPARATOR));
		dao.update(user);
		return UserResponse.build(user);
	}

}
