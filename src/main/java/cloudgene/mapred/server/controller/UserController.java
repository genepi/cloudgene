package cloudgene.mapred.server.controller;

import java.util.List;

import javax.validation.constraints.NotBlank;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.auth.AuthenticationService;
import cloudgene.mapred.server.exceptions.JsonHttpStatusException;
import cloudgene.mapred.server.responses.UserResponse;
import cloudgene.mapred.server.services.UserService;
import cloudgene.mapred.util.Page;
import cloudgene.mapred.util.PageUtil;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import jakarta.inject.Inject;
import net.sf.json.JSONObject;

@Controller
public class UserController {

	public static final String MESSAGE_USER_NOT_FOUND = "User %s not found.";

	public static final int DEFAULT_PAGE_SIZE = 100;

	@Inject
	protected Application application;

	@Inject
	protected UserService userService;

	@Inject
	protected AuthenticationService authenticationService;

	@Get("/api/v2/admin/users")
	@Secured(User.ROLE_ADMIN)
	public String get(@Nullable @QueryValue("page") String page, @Nullable @QueryValue("query") String query) {

		int pageSize = DEFAULT_PAGE_SIZE;

		Page<User> users = userService.getAll(query, page, pageSize);

		List<UserResponse> userResponses = UserResponse.build(users.getData());

		JSONObject object = PageUtil.createPageObject(users);
		object.put("data", userResponses);

		return object.toString();

	}

	@Post("/api/v2/admin/users/{username}/delete")
	@Secured(User.ROLE_ADMIN)
	public UserResponse delete(@PathVariable @NotBlank String username) {

		// delete user from database
		UserDao dao = new UserDao(application.getDatabase());
		User user = dao.findByUsername(username);

		if (user == null) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, String.format(MESSAGE_USER_NOT_FOUND, username));
		}

		dao.delete(user);

		return UserResponse.build(user);
	}

	@Post("/api/v2/admin/users/changegroup")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Secured(User.ROLE_ADMIN)
	public UserResponse changeGroup(@NotBlank String username, @NotBlank String role) {

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
