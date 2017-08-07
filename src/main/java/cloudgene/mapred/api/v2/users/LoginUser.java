package cloudgene.mapred.api.v2.users;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.JWTUtil;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.representations.JSONAnswer;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.HashUtil;

public class LoginUser extends BaseResource {

	public static final int MAX_LOGIN_ATTEMMPTS = 5;

	public static final int LOCKING_TIME_MIN = 30;

	@Post
	public Representation post(Representation entity) {
		Form form = new Form(entity);

		String username = form.getFirstValue("loginUsername");
		String password = form.getFirstValue("loginPassword");
		password = HashUtil.getMD5(password);

		UserDao dao = new UserDao(getDatabase());
		User user = dao.findByUsername(username);

		if (user != null) {

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

			if (user.getPassword().equals(password)) {

				// create unique csrf token
				String csrfToken = HashUtil.getMD5("csrf_token_for_" + user.getUsername());

				// create cookie token with crf token
				String token = JWTUtil.createCookieToken(user, getSettings().getSecretKey(), csrfToken);
				// set cookie
				CookieSetting cookie = new CookieSetting(JWTUtil.COOKIE_NAME, token);

				if ((getSettings().isHttps()) || getSettings().isSecureCookie()) {
					cookie.setSecure(true);
					cookie.setAccessRestricted(true);
				}

				getResponse().getCookieSettings().add(cookie);
				user.setLoginAttempts(0);
				user.setLastLogin(new Date());
				dao.update(user);

				JSONObject answer = new JSONObject();
				try {
					answer.put("success", true);
					answer.put("message", "Login successfull.");
					answer.put("csrf", csrfToken);
					answer.put("type", "plain");
				} catch (JSONException e) {
					e.printStackTrace();
				}

				return new JsonRepresentation(answer);

			} else {

				// count failed logins
				int attempts = user.getLoginAttempts();
				attempts++;
				user.setLoginAttempts(attempts);

				// too many, lock user
				if (attempts >= MAX_LOGIN_ATTEMMPTS) {
					user.setLockedUntil(new Date(System.currentTimeMillis() + (LOCKING_TIME_MIN * 60 * 1000)));
				}
				dao.update(user);

				return new JSONAnswer("Login Failed! Wrong Username or Password.", false);
			}
		} else {
			return new JSONAnswer("Login Failed! Wrong Username or Password.", false);
		}
	}

}
