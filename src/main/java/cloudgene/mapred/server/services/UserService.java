package cloudgene.mapred.server.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloudgene.mapred.core.Template;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.exceptions.JsonHttpStatusException;
import cloudgene.mapred.server.responses.MessageResponse;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.MailUtil;
import cloudgene.mapred.util.Page;
import io.micronaut.http.HttpStatus;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class UserService {
	
	private static Logger log = LoggerFactory.getLogger(UserService.class);

	public static final String MESSAGE_USER_NOT_FOUND = "User %s not found.";

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

	public static final String DEFAULT_ROLE = "User";

	@Inject
	protected Application application;

	public Page<User> getAll(String query, String page, int pageSize) {

		int offset = 0;
		if (page != null) {

			offset = Integer.valueOf(page);
			if (offset < 1) {
				offset = 1;
			}
			offset = (offset - 1) * pageSize;
		}

		UserDao dao = new UserDao(application.getDatabase());

		List<User> users = null;
		int count = 0;

		if (query != null && !query.isEmpty()) {
			users = dao.findByQuery(query);
			page = "1";
			count = users.size();
			pageSize = count;
		} else {
			if (page != null) {
				users = dao.findAll(offset, pageSize);
				count = dao.findAll().size();
			} else {
				users = dao.findAll();
				page = "1";
				count = users.size();
				pageSize = count;
			}
		}

		Page<User> result = new Page<User>();
		result.setCount(count);
		result.setPage(Integer.parseInt(page));
		result.setPageSize(pageSize);
		result.setData(users);

		return result;

	}

	public User getByUsername(String username) {
		UserDao dao = new UserDao(application.getDatabase());
		User user = dao.findByUsername(username);
		if (user == null) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, String.format(MESSAGE_USER_NOT_FOUND, username));
		}
		return user;
	}

	public User deleteUser(User user) {
		UserDao dao = new UserDao(application.getDatabase());
		dao.delete(user);
		return user;
	}

	public User changeRoles(User user, String roles) {
		UserDao dao = new UserDao(application.getDatabase());
		user.setRoles(roles.split(User.ROLE_SEPARATOR));
		dao.update(user);
		return user;
	}

	public MessageResponse updateProfile(User user, String username, String full_name, String mail, String new_password,
			String confirm_new_password) {

		String error = User.checkUsername(username);
		if (error != null) {
			return MessageResponse.error(error);
		}

		// check if user is admin or it is his username
		if (!user.getUsername().equals(username) && !user.isAdmin()) {
			
			log.error(String.format("User: ID %s ('%s') attempted to change profile of a different user '%s'",
					user.getId(), user.getUsername(), username));
			
			return MessageResponse.error(MESSAGE_NOT_ALLOWED);
		}

		error = User.checkName(full_name);
		if (error != null) {
			return MessageResponse.error(error);
		}

		error = User.checkMail(mail);
		if (error != null) {
			return MessageResponse.error(error);
		}

		UserDao dao = new UserDao(application.getDatabase());
		User newUser = dao.findByUsername(username);
		newUser.setFullName(full_name);
		newUser.setMail(mail);

		if (!user.getMail().equals(newUser.getMail())) {
			log.info(String.format("User: changed email address for user %s (ID %s)", newUser.getUsername(),
					newUser.getId()));
		}
		
		// update password only when it's not empty
		if (new_password != null && !new_password.isEmpty()) {

			error = User.checkPassword(new_password, confirm_new_password);

			if (error != null) {
				return MessageResponse.error(error);
			}
			newUser.setPassword(HashUtil.hashPassword(new_password));

			log.info(String.format("User: changed password for user %s (ID %s - email %s)", newUser.getUsername(),
					newUser.getId(), newUser.getMail()));
			
		}

		dao.update(newUser);

		return MessageResponse.success(MESSAGE_PROFILE_UPDATED);
	}

	public MessageResponse deleteProfile(User user, String username, String password) {

		// check if user is admin or it is his username
		if (!user.getUsername().equals(username) && !user.isAdmin()) {
			throw new JsonHttpStatusException(HttpStatus.FORBIDDEN, MESSAGE_NOT_ALLOWED);
		}

		if (HashUtil.checkPassword(password, user.getPassword())) {

			UserDao dao = new UserDao(application.getDatabase());
			
			log.info(String.format("User: requested deletion of account %s (ID %s - email %s)", user.getUsername(),
					user.getId(), user.getMail()));
			
			boolean deleted = dao.delete(user);
			if (deleted) {
				return MessageResponse.success(MESSAGE_USER_PROFILE_DELETE);
			} else {
				throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST, MESSAGE_DELETE_ERROR);
			}

		} else {
			throw new JsonHttpStatusException(HttpStatus.UNAUTHORIZED, MESSAGE_WRONG_PASSWORD);
		}

	}

	public MessageResponse updatePassword(String username, String token, String new_password,
			String confirm_new_password) {

		if (username == null || username.isEmpty()) {
			return MessageResponse.error(MESSAGE_NO_USERNAME_SET);
		}

		UserDao dao = new UserDao(application.getDatabase());
		User user = dao.findByUsername(username);

		if (user == null) {
			return MessageResponse.error(MESSAGE_ACCOUNT_NOT_FOUND);
		}

		if (!user.isActive()) {
			return MessageResponse.error(MESSAGE_ACCOUNT_IS_INACTIVE);
		}

		if (token == null || user.getActivationCode() == null || !user.getActivationCode().equals(token)) {
			return MessageResponse.error(MESSAGE_INVALID_RECOVERY_REQUEST);
		}

		String error = User.checkPassword(new_password, confirm_new_password);
		if (error != null) {
			return MessageResponse.error(error);
		}

		user.setPassword(HashUtil.hashPassword(new_password));
		user.setActivationCode("");
		dao.update(user);

		log.info(String.format("User: changed password via account recovery mechanism for user %s (ID %s - email %s)",
				user.getUsername(), user.getId(), user.getMail()));

		return MessageResponse.success(MESSAGE_PASSWORD_UPDATED);
	}

	public MessageResponse resetPassword(String username) {

		if (username == null || username.isEmpty()) {
			return MessageResponse.error(MESSAGE_INVALID_USERNAME);
		}

		UserDao dao = new UserDao(application.getDatabase());
		User user = dao.findByUsername(username);

		if (user == null) {
			user = dao.findByMail(username);
		}

		if (user != null) {

			if (!user.isActive()) {
				return MessageResponse.error(MESSAGE_ACCOUNT_IS_INACTIVE);
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

			String hostname = application.getSettings().getServerUrl();
			hostname += application.getSettings().getUrlPrefix();

			String link = hostname + "/#!recovery/" + user.getUsername() + "/" + key;

			// send email with activation code
			String app = application.getSettings().getName();
			String subject = "[" + app + "] Password Recovery";
			String body = application.getTemplate(Template.RECOVERY_MAIL, user.getFullName(), application, link);

			try {
				log.info(String.format("Password reset link requested for user '%s'", username));

				MailUtil.send(application.getSettings(), user.getMail(), subject, body);

				return MessageResponse.success(String.format(MESSAGE_EMAIL_SENT, user.getMail()));

			} catch (Exception e) {

				return MessageResponse.error(MESSAGE_SENDING_EMAIL_FAILED + e.getMessage());

			}

		} else {

			return MessageResponse.error(MESSAGE_ACCOUNT_NOT_FOUND);

		}

	}

	public MessageResponse registerUser(String username, String mail, String new_password, String confirm_new_password,
			String full_name) {
		// check username
		String error = User.checkUsername(username);
		if (error != null) {
			return MessageResponse.error(error);
		}
		UserDao dao = new UserDao(application.getDatabase());
		if (dao.findByUsername(username) != null) {
			return MessageResponse.error(MESSAGE_USERNAME_ALREADY_EXISTS);
		}

		// check email
		error = User.checkMail(mail);
		if (error != null) {
			return MessageResponse.error(error);
		}
		if (dao.findByMail(mail) != null) {
			return MessageResponse.error(MESAGE_EMAIL_ALREADY_REGISTERED);
		}

		// check password
		error = User.checkPassword(new_password, confirm_new_password);
		if (error != null) {
			return MessageResponse.error(error);
		}

		// check password
		error = User.checkName(full_name);
		if (error != null) {
			return MessageResponse.error(error);
		}

		User newUser = new User();
		newUser.setUsername(username);
		newUser.setFullName(full_name);
		newUser.setMail(mail);
		newUser.setRoles(new String[] { UserService.DEFAULT_ROLE });
		newUser.setPassword(HashUtil.hashPassword(new_password));

		try {

			String hostname = application.getSettings().getServerUrl();
			hostname += application.getSettings().getUrlPrefix();

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

			log.info(String.format("Registration: New user %s (ID %s - email %s)", username, newUser.getId(),
					newUser.getMail()));

			dao.insert(newUser);

			return MessageResponse.success(MESSAGE_USER_CREATED);

		} catch (Exception e) {

			return MessageResponse.error(e.getMessage());

		}
	}

	public MessageResponse activateUser(String username, String code) {
		UserDao dao = new UserDao(application.getDatabase());
		User user = dao.findByUsername(username);

		if (user != null) {

			if (user.getActivationCode() != null && user.getActivationCode().equals(code)) {

				user.setActive(true);
				user.setActivationCode("");
				dao.update(user);

				log.info(String.format("User: activated user %s (ID %s - email %s)", user.getUsername(), user.getId(),
						user.getMail()));

				return MessageResponse.success(MESSAGE_USER_ACTIVATED);

			} else {

				log.warn(String.format(
						"User: code is either incorrect or has already been used for user %s (ID %s - email %s)",
						user.getUsername(), user.getId(), user.getMail()));

				return MessageResponse.error(MESSAGE_WRONG_ACTIVATION_CODE);

			}

		} else {

			log.warn(String.format("User: used activation code for missing or unknown username '%s'", username));

			return MessageResponse.error(MESSAGE_WRONG_USERNAME);

		}
	}
}
