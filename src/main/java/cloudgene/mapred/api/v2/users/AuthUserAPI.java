package cloudgene.mapred.api.v2.users;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.JWTUtil;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.representations.JSONAnswer;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.HashUtil;

public class AuthUserAPI extends BaseResource {

	@Post
	public Representation post(Representation entity) {
		Form form = new Form(entity);

		String username = form.getFirstValue("username");
		String password = form.getFirstValue("password");
		password = HashUtil.getMD5(password);

		if (username == null || password == null) {
			return error(Status.CLIENT_ERROR_BAD_REQUEST, "Wrong parameters.");
		}

		UserDao dao = new UserDao(getDatabase());
		User user = dao.findByUsername(username);

		if (user != null) {

			if (!user.isActive()) {
				return new JSONAnswer("Login Failed! User account is not activated.", false);
			}

			if (user.getLoginAttempts() >= LoginUser.MAX_LOGIN_ATTEMMPTS) {
				if (user.getLockedUntil() == null || user.getLockedUntil().after(new Date())) {
					return new JSONAnswer("The user account is locked for " + LoginUser.LOCKING_TIME_MIN
							+ " minutes. Too many failed logins.", false);
				} else {
					// penalty time is over. set to zero
					user.setLoginAttempts(0);
				}
			}

			if (user.getPassword().equals(password)) {

				// create session
				String token = JWTUtil.createToken(user, getSettings().getSecretKey());
				// set cookie
				CookieSetting cookie = new CookieSetting(JWTUtil.COOKIE_NAME, token);
				getResponse().getCookieSettings().add(cookie);
				user.setLoginAttempts(0);
				user.setLastLogin(new Date());
				dao.update(user);

				return new JSONAnswer("Login successfull.", true);

			} else {

				// count failed logins
				int attempts = user.getLoginAttempts();
				attempts++;
				user.setLoginAttempts(attempts);

				// too many, lock user
				if (attempts >= LoginUser.MAX_LOGIN_ATTEMMPTS) {
					user.setLockedUntil(
							new Date(System.currentTimeMillis() + (LoginUser.LOCKING_TIME_MIN * 60 * 1000)));
				}
				dao.update(user);

				return new JSONAnswer("Login Failed! Wrong Username or Password.", false);
			}
		} else {
			return new JSONAnswer("Login Failed! Wrong Username or Password.", false);
		}
	}

}
