package cloudgene.mapred.api.v2.users;

import java.util.Date;

import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;

import cloudgene.mapred.core.JWTUtil;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.representations.JSONAnswer;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.HashUtil;

public class OAuth extends BaseResource {

	public static final int MAX_LOGIN_ATTEMMPTS = 5;

	public static final int LOCKING_TIME_MIN = 30;

	@Post
	public Representation createApiKey(Representation entity) {

		Form form = new Form(entity);

		String username = form.getFirstValue("username");
		String password = form.getFirstValue("password");
		password = HashUtil.getMD5(password);

		UserDao dao = new UserDao(getDatabase());
		User user = dao.findByUsername(username);

		if (user == null) {
			return error401("Login Failed! Wrong Username or Password.");
		}

		if (!user.isActive()) {
			return new JSONAnswer("Login Failed! User account is not activated.", false);
		}

		if (user.getLoginAttempts() >= MAX_LOGIN_ATTEMMPTS) {
			if (user.getLockedUntil() == null || user.getLockedUntil().after(new Date())) {
				return new JSONAnswer("The user account is locked for " + LoginUser.LOCKING_TIME_MIN
						+ " minutes. Too many failed logins.", false);
			} else {
				// penalty time is over. set to zero
				user.setLoginAttempts(0);
			}
		}

		if (!user.getPassword().equals(password)) {
			// count failed logins
			int attempts = user.getLoginAttempts();
			attempts++;
			user.setLoginAttempts(attempts);

			// too many, lock user
			if (attempts >= MAX_LOGIN_ATTEMMPTS) {
				user.setLockedUntil(new Date(System.currentTimeMillis() + (LOCKING_TIME_MIN * 60 * 1000)));
			}
			dao.update(user);
			return error401("Login Failed! Wrong Username or Password.");
		}

		// create token
		String token = JWTUtil.createApiToken(user, getSettings().getSecretKey());

		// update token
		UserDao userDao = new UserDao(getDatabase());
		boolean successful = userDao.update(user);

		if (successful) {

			// return token
			JSONObject answer = new JSONObject();
			answer.put("success", true);
			answer.put("message", "Creation successfull.");
			answer.put("access_token", token);
			answer.put("username", username);
			answer.put("type", "plain");
			return new StringRepresentation(answer.toString());

		} else {

			return new JSONAnswer("Error during API token generation.", false);

		}

	}

}
