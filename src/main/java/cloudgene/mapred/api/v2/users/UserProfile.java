package cloudgene.mapred.api.v2.users;

import cloudgene.mapred.Application;
import cloudgene.mapred.auth.AuthenticationService;
import cloudgene.mapred.auth.AuthenticationType;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.exceptions.JsonHttpStatusException;
import cloudgene.mapred.responses.MessageResponse;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.JSONConverter;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import net.sf.json.JSONObject;

@Controller
public class UserProfile {

	private static final String MESSAGE_USER_PROFILE_DELETE = "User profile sucessfully delete.";

	private static final String MESSAGE_DELETE_ERROR = "Error during deleting your user profile.";

	private static final String MESSAGE_WRONG_PASSWORD = "Wrong password.";

	private static final String MESSAGE_PROFILE_UPDATED = "User profile sucessfully updated.";

	private static final String MESSAGE_NOT_ALLOWED = "You are not allowed to change this user profile.";

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Get("/api/v2/users/{user2}/profile")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public String get(Authentication authentication, String user2) {

		User user = authenticationService.getUserByAuthentication(authentication, AuthenticationType.ALL_TOKENS);

		UserDao dao = new UserDao(application.getDatabase());
		User updatedUser = dao.findByUsername(user.getUsername());

		// TODO: Create UserRepsonse object instead of JSONConverter

		JSONObject object = JSONConverter.convert(updatedUser);
		try {
			if (object.getBoolean("hasApiToken")) {
				// org.json.JSONObject result = ApiToken.verify(user.getApiToken(),
				// getSettings().getSecretKey(),
				// getDatabase());
				// object.put("apiTokenValid", result.get("valid"));
				// object.put("apiTokenMessage", result.get("message"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return object.toString();

	}

	@Post("/api/v2/users/{user2}/profile")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public HttpResponse<MessageResponse> update(Authentication authentication, String user2, @Nullable String username,
			String full_name, String mail, String new_password, String confirm_new_password) {

		User user = authenticationService.getUserByAuthentication(authentication);

		String error = User.checkUsername(username);
		if (error != null) {
			return HttpResponse.ok(MessageResponse.error(error));
		}

		// check if user is admin or it is his username
		if (!user.getUsername().equals(username) && !user.isAdmin()) {
			return HttpResponse.ok(MessageResponse.error(MESSAGE_NOT_ALLOWED));
		}

		error = User.checkName(full_name);
		if (error != null) {
			return HttpResponse.ok(MessageResponse.error(error));
		}

		error = User.checkMail(mail);
		if (error != null) {
			return HttpResponse.ok(MessageResponse.error(error));
		}

		UserDao dao = new UserDao(application.getDatabase());
		User newUser = dao.findByUsername(username);
		newUser.setFullName(full_name);
		newUser.setMail(mail);

		// update password only when it's not empty
		if (new_password != null && !new_password.isEmpty()) {

			error = User.checkPassword(new_password, confirm_new_password);

			if (error != null) {
				return HttpResponse.ok(MessageResponse.error(error));
			}
			newUser.setPassword(HashUtil.hashPassword(new_password));

		}

		dao.update(newUser);

		return HttpResponse.ok(MessageResponse.success(MESSAGE_PROFILE_UPDATED));

	}

	@Delete("/api/v2/users/{user2}/profile")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public HttpResponse<MessageResponse> delete(Authentication authentication, String user2, String username,
			String password) {

		User user = authenticationService.getUserByAuthentication(authentication);

		// check if user is admin or it is his username
		if (!user.getUsername().equals(username) && !user.isAdmin()) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, MESSAGE_NOT_ALLOWED);
		}

		if (HashUtil.checkPassword(password, user.getPassword())) {

			UserDao dao = new UserDao(application.getDatabase());
			boolean deleted = dao.delete(user);
			if (deleted) {
				return HttpResponse.ok(MessageResponse.success(MESSAGE_USER_PROFILE_DELETE));
			} else {
				throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST, MESSAGE_DELETE_ERROR);
			}

		} else {
			throw new JsonHttpStatusException(HttpStatus.UNAUTHORIZED, MESSAGE_WRONG_PASSWORD);
		}

	}

}
