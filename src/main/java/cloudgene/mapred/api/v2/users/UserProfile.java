package cloudgene.mapred.api.v2.users;

import cloudgene.mapred.Application;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.representations.JSONAnswer;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.JSONConverter;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import net.sf.json.JSONObject;

@Controller
public class UserProfile {

	@Inject
	protected Application application;

	@Get("/api/v2/users/{user2}/profile")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public String get(Authentication authentication, String user2) {

		User user = application.getUserByAuthentication(authentication);

		if (user == null) {
			throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "The request requires user authentication.");
		}

		UserDao dao = new UserDao(application.getDatabase());
		User updatedUser = dao.findByUsername(user.getUsername());

		JSONObject object = JSONConverter.convert(updatedUser);
		try {
			if (object.getBoolean("hasApiToken")) {
				//org.json.JSONObject result = ApiToken.verify(user.getApiToken(), getSettings().getSecretKey(),
				//		getDatabase());
				//object.put("apiTokenValid", result.get("valid"));
				//object.put("apiTokenMessage", result.get("message"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return object.toString();

	}

	@Post(uri = "/api/v2/users/{user2}/profile", consumes = MediaType.APPLICATION_FORM_URLENCODED)
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public String post(Authentication authentication, String user2, String username, String full_name, String mail, String new_password, String confirm_new_password) {

		User user = application.getUserByAuthentication(authentication);

		if (user == null) {
			throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "The request requires user authentication.");
		}


		String error = User.checkUsername(username);
		if (error != null) {
			return new JSONAnswer(error, false).toString();
		}

		// check if user is admin or it is his username
		if (!user.getUsername().equals(username) && !user.isAdmin()) {
			return new JSONAnswer("You are not allowed to change this user profile.", false).toString();
		}

		error = User.checkName(full_name);
		if (error != null) {
			return new JSONAnswer(error, false).toString();
		}

		error = User.checkMail(mail);
		if (error != null) {
			return new JSONAnswer(error, false).toString();
		}

		UserDao dao = new UserDao(application.getDatabase());
		User newUser = dao.findByUsername(username);
		newUser.setFullName(full_name);
		newUser.setMail(mail);

		// update password only when it's not empty
		if (new_password != null && !new_password.isEmpty()) {

			error = User.checkPassword(new_password, confirm_new_password);

			if (error != null) {
				return new JSONAnswer(error, false).toString();
			}
			newUser.setPassword(HashUtil.hashPassword(new_password));

		}

		dao.update(newUser);

		return new JSONAnswer("User profile sucessfully updated.", true).toString();

	}

	@Delete("/api/v2/users/{user2}/profile")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public String delete(Authentication authentication, String user2, String username, String password) {

		User user = application.getUserByAuthentication(authentication);

		if (user == null) {
			throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "The request requires user authentication.");
		}

		// check if user is admin or it is his username
		if (!user.getUsername().equals(username) && !user.isAdmin()) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND, "You are not allowed to delete this user profile.");
		}

		if (HashUtil.checkPassword(password, user.getPassword())) {

			UserDao dao = new UserDao(application.getDatabase());
			boolean deleted = dao.delete(user);
			if (deleted) {
				return new JSONAnswer("User profile sucessfully delete.", true).toString();
			} else {
				throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Error during deleting your user profile.");
			}

		} else {
			throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "Wrong password.");
		}

	}

}
