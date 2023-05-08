package cloudgene.mapred.server.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.auth.AuthenticationService;
import cloudgene.mapred.server.auth.AuthenticationType;
import cloudgene.mapred.server.responses.MessageResponse;
import cloudgene.mapred.server.responses.UserResponse;
import cloudgene.mapred.server.services.UserService;
import cloudgene.mapred.util.Page;
import cloudgene.mapred.util.PageUtil;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import net.sf.json.JSONObject;

@Controller
public class UserController {

	private static Logger log = LoggerFactory.getLogger(UserController.class);

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
		Page<User> users = userService.getAll(query, page, DEFAULT_PAGE_SIZE);
		List<UserResponse> userResponses = UserResponse.build(users.getData());
		JSONObject object = PageUtil.createPageObject(users);
		object.put("data", userResponses);
		return object.toString();

	}

	@Post("/api/v2/admin/users/{username}/delete")
	@Secured(User.ROLE_ADMIN)
	public UserResponse delete(Authentication authentication, String username) {

		User admin = authenticationService.getUserByAuthentication(authentication);

		User user = userService.getByUsername(username);
		user = userService.deleteUser(user);

		log.info(String.format("User: Deleted user %s (ID %s) (by ADMIN user ID %s - email %s)", user.getUsername(),
				user.getId(), admin.getId(), admin.getMail()));

		return UserResponse.build(user);
	}

	@Post("/api/v2/admin/users/changegroup")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Secured(User.ROLE_ADMIN)
	public UserResponse changeGroup(Authentication authentication, String username, String role) {

		User admin = authenticationService.getUserByAuthentication(authentication);

		User user = userService.getByUsername(username);
		user = userService.changeRoles(user, role);

		log.info(String.format("User: Changed group membership for %s (ID %s) to %s (by ADMIN user ID %s - email %s)",
				user.getUsername(), user.getId(), String.join(",", user.getRoles()), admin.getId(), admin.getMail()));

		return UserResponse.build(user);
	}

	@Get("/api/v2/users/{user2}/profile")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public UserResponse get(Authentication authentication, String user2) {

		User user = authenticationService.getUserByAuthentication(authentication, AuthenticationType.ALL_TOKENS);

		UserDao dao = new UserDao(application.getDatabase());
		User updatedUser = dao.findByUsername(user.getUsername());

		return UserResponse.build(updatedUser);

	}

	@Post("/api/v2/users/{user2}/profile")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public HttpResponse<MessageResponse> update(Authentication authentication, String user2, @Nullable String username,
			String full_name, String mail, String new_password, String confirm_new_password) {

		User user = authenticationService.getUserByAuthentication(authentication);

		MessageResponse response = userService.updateProfile(user, username, full_name, mail, new_password,
				confirm_new_password);

		return HttpResponse.ok(response);

	}

	@Delete("/api/v2/users/{username}/profile")
	@Consumes(MediaType.ALL)
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public HttpResponse<MessageResponse> delete(Authentication authentication, String username,
			@QueryValue String password) {

		User user = authenticationService.getUserByAuthentication(authentication);

		MessageResponse response = userService.deleteProfile(user, username, password);

		return HttpResponse.ok(response);

	}

	@Post("/api/v2/users/update-password")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Secured(SecurityRule.IS_ANONYMOUS)
	public HttpResponse<MessageResponse> updatePassword(String token, @Nullable String username, String new_password,
			String confirm_new_password) {

		MessageResponse response = userService.updatePassword(username, token, new_password, confirm_new_password);

		return HttpResponse.ok(response);

	}

	@Post("/api/v2/users/reset")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Secured(SecurityRule.IS_ANONYMOUS)
	public HttpResponse<MessageResponse> resetPassword(@Nullable String username) {
		MessageResponse response = userService.resetPassword(username);

		return HttpResponse.ok(response);

	}

	@Post("/api/v2/users/register")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Secured(SecurityRule.IS_ANONYMOUS)
	public HttpResponse<MessageResponse> register(String username, String full_name, String mail, String new_password,
			String confirm_new_password) {

		MessageResponse response = userService.registerUser(username, mail, new_password, confirm_new_password,
				full_name);
		return HttpResponse.ok(response);

	}

	@Get("/users/activate/{username}/{code}")
	@Secured(SecurityRule.IS_ANONYMOUS)
	public HttpResponse<MessageResponse> activate(String username, String code) {

		MessageResponse response = userService.activateUser(username, code);
		return HttpResponse.ok(response);

	}

}
