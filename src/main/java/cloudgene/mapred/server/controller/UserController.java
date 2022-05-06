package cloudgene.mapred.server.controller;

import java.util.List;

import cloudgene.mapred.core.Template;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.auth.AuthenticationService;
import cloudgene.mapred.server.auth.AuthenticationType;
import cloudgene.mapred.server.exceptions.JsonHttpStatusException;
import cloudgene.mapred.server.responses.MessageResponse;
import cloudgene.mapred.server.responses.UserResponse;
import cloudgene.mapred.server.services.UserService;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.MailUtil;
import cloudgene.mapred.util.Page;
import cloudgene.mapred.util.PageUtil;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
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

	public static final int DEFAULT_PAGE_SIZE = 100;

	private static final String MESSAGE_USER_PROFILE_DELETE = "User profile sucessfully delete.";

	private static final String MESSAGE_DELETE_ERROR = "Error during deleting your user profile.";

	private static final String MESSAGE_WRONG_PASSWORD = "Wrong password.";

	private static final String MESSAGE_PROFILE_UPDATED = "User profile sucessfully updated.";

	private static final String MESSAGE_NOT_ALLOWED = "You are not allowed to change this user profile.";

	private static final String MESSAGE_NO_USERNAME_SET = "No username set.";

	private static final String MESSAGE_PASSWORD_UPDATED = "Password sucessfully updated.";

	private static final String MESSAGE_INVALID_RECOVERY_REQUEST = "Your recovery request is invalid or expired.";

	private static final String MESSAGE_ACCOUNT_IS_INACTIVE = "Account is not activated.";

	private static final String MESSAGE_ACCOUNT_NOT_FOUND = "We couldn't find an account with that username or email.";
	
	private static final String MESSAGE_EMAIL_SENT = "Email sent to %s with instructions on how to reset your password.";

	private static final String MESSAGE_SENDING_EMAIL_FAILED = "Sending recovery email failed. ";

	private static final String MESSAGE_INVALID_USERNAME = "Please enter a valid username or email address.";
	
	private static final String MESSAGE_USER_CREATED = "User sucessfully created.";

	private static final String MESAGE_EMAIL_ALREADY_REGISTERED = "E-Mail is already registered.";

	private static final String MESSAGE_USERNAME_ALREADY_EXISTS = "Username already exists.";

	private static final String MESSAGE_WRONG_USERNAME = "Wrong username.";

	private static final String MESSAGE_WRONG_ACTIVATION_CODE = "Wrong activation code.";

	private static final String MESSAGE_USER_ACTIVATED = "User sucessfully activated.";

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
	public UserResponse delete(String username) {
		User user = userService.getByUsername(username);
		user = userService.deleteUser(user);
		return UserResponse.build(user);
	}

	@Post("/api/v2/admin/users/changegroup")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Secured(User.ROLE_ADMIN)
	public UserResponse changeGroup(String username, String role) {
		User user = userService.getByUsername(username);
		user = userService.changeRoles(user, role);
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
			throw new JsonHttpStatusException(HttpStatus.FORBIDDEN, MESSAGE_NOT_ALLOWED);
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

	@Post("/api/v2/users/update-password")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Secured(SecurityRule.IS_ANONYMOUS)
	public HttpResponse<MessageResponse> updatePassword(String token, @Nullable String username, String new_password,
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
	
	@Post("/api/v2/users/reset")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Secured(SecurityRule.IS_ANONYMOUS)
	public HttpResponse<MessageResponse> resetPassword(@Nullable String username) {

		if (username == null || username.isEmpty()) {
			return HttpResponse.ok(MessageResponse.error(MESSAGE_INVALID_USERNAME));
		}

		UserDao dao = new UserDao(application.getDatabase());
		User user = dao.findByUsername(username);

		if (user == null) {
			user = dao.findByMail(username);
		}

		if (user != null) {

			if (!user.isActive()) {
				return HttpResponse.ok(MessageResponse.error(MESSAGE_ACCOUNT_IS_INACTIVE));
			}

			String key = "";
			if (user.getActivationCode() != null && !user.getActivationCode().isEmpty()) {

				// resend the same activation token
				key = user.getActivationCode();

			} else {

				// create activation token
				key = HashUtil.getActivationHash(user);
				user.setActivationCode(key);
				dao.update(user);
			}

			String hostname = application.getSettings().getHostname();

			String link = hostname + "/#!recovery/" + user.getUsername() + "/" + key;

			// send email with activation code
			String app = application.getSettings().getName();
			String subject = "[" + app + "] Password Recovery";
			String body = application.getTemplate(Template.RECOVERY_MAIL, user.getFullName(), application, link);

			try {

				MailUtil.notifySlack(application.getSettings(), "Hi! " + username + " asked for a new password :key:");

				MailUtil.send(application.getSettings(), user.getMail(), subject, body);

				return HttpResponse.ok(MessageResponse.success(String.format(MESSAGE_EMAIL_SENT, user.getMail())));

			} catch (Exception e) {

				return HttpResponse.ok(MessageResponse.error(MESSAGE_SENDING_EMAIL_FAILED + e.getMessage()));

			}

		} else {

			return HttpResponse.ok(MessageResponse.error(MESSAGE_ACCOUNT_NOT_FOUND));

		}

	}
	
	@Post("/api/v2/users/register")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Secured(SecurityRule.IS_ANONYMOUS)
	public HttpResponse<MessageResponse> register(String username, String full_name, String mail, String new_password,
			String confirm_new_password) {

		// check username
		String error = User.checkUsername(username);
		if (error != null) {
			return HttpResponse.ok(MessageResponse.error(error));
		}
		UserDao dao = new UserDao(application.getDatabase());
		if (dao.findByUsername(username) != null) {
			return HttpResponse.ok(MessageResponse.error(MESSAGE_USERNAME_ALREADY_EXISTS));
		}

		// check email
		error = User.checkMail(mail);
		if (error != null) {
			return HttpResponse.ok(MessageResponse.error(error));
		}
		if (dao.findByMail(mail) != null) {
			return HttpResponse.ok(MessageResponse.error(MESAGE_EMAIL_ALREADY_REGISTERED));
		}

		// check password
		error = User.checkPassword(new_password, confirm_new_password);
		if (error != null) {
			return HttpResponse.ok(MessageResponse.error(error));
		}

		// check password
		error = User.checkName(full_name);
		if (error != null) {
			return HttpResponse.ok(MessageResponse.error(error));
		}

		User newUser = new User();
		newUser.setUsername(username);
		newUser.setFullName(full_name);
		newUser.setMail(mail);
		newUser.setRoles(new String[] { UserService.DEFAULT_ROLE });
		newUser.setPassword(HashUtil.hashPassword(new_password));

		try {

			String hostname = application.getSettings().getHostname();

			// if email server configured, send mails with activation link. Else
			// activate user immediately.

			if (application.getSettings().getMail() != null) {

				String activationKey = HashUtil.getActivationHash(newUser);
				newUser.setActive(false);
				newUser.setActivationCode(activationKey);

				// send email with activation code
				String appName = application.getSettings().getName();
				String subject = "[" + appName + "] Signup activation";
				String activationLink = hostname + "/#!activate/" + username + "/" + activationKey;
				String body = application.getTemplate(Template.REGISTER_MAIL, full_name, application, activationLink);

				MailUtil.send(application.getSettings(), mail, subject, body);

			} else {

				newUser.setActive(true);
				newUser.setActivationCode("");

			}

			MailUtil.notifySlack(application.getSettings(),
					"Hi! say hello to " + username + " (" + mail + ") :hugging_face:");

			dao.insert(newUser);

			return HttpResponse.ok(MessageResponse.success(MESSAGE_USER_CREATED));

		} catch (Exception e) {

			return HttpResponse.ok(MessageResponse.error(e.getMessage()));

		}

	}
	
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
