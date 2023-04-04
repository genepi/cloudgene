package cloudgene.mapred.api.v2.users;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

import cloudgene.mapred.core.JWTUtil;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.representations.JSONAnswer;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.HashUtil;

public class LoginUser extends BaseResource {

	private static final Log log = LogFactory.getLog(LoginUser.class);

	public static final int MAX_LOGIN_ATTEMPTS = 5;

	public static final int LOCKING_TIME_MIN = 30;

	@Post
	public Representation post(Representation entity) {
		Form form = new Form(entity);

		String username = form.getFirstValue("loginUsername");
		String password = form.getFirstValue("loginPassword");
		//password = HashUtil.getMD5(password);

		UserDao dao = new UserDao(getDatabase());
		User user = dao.findByUsername(username);

		if (user != null) {

			if (!user.isActive()) {
				log.info(String.format("Authorization failure: User account is not activated for account %s (ID %s - email %s)", user.getUsername(), user.getId(), user.getMail()));
				return new JSONAnswer("Login Failed! User account is not activated.", false);
			}

			if (user.getLoginAttempts() >= MAX_LOGIN_ATTEMPTS) {
				if (user.getLockedUntil() == null || user.getLockedUntil().after(new Date())) {
					log.info(String.format("Authorization failure: login retries are currently locked for account %s (ID %s - email %s)", user.getUsername(), user.getId(), user.getMail()));
					return new JSONAnswer("The user account is locked for " + LoginUser.LOCKING_TIME_MIN
							+ " minutes. Too many failed logins.", false);
				} else {
					// penalty time is over. set to zero
					log.info(String.format("Authorization: Account login lock has expired; releasing for account %s (ID %s - email %s)", user.getUsername(), user.getId(), user.getMail()));
					user.setLoginAttempts(0);
				}
			}

			if (HashUtil.checkPassword(password, user.getPassword())) {

				// create unique csrf token
				String csrfToken = HashUtil.getCsrfToken(user);

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
					log.error("Authorization: Unexpected error in serializing login tokens", e);
					e.printStackTrace();
				}


				String message = String.format("Authorization success: user login %s (ID %s - email %s)", user.getUsername(), user.getId(), user.getMail());
				if (user.isAdmin()) {
					// Note: Admin user logins are called out explicitly, to aid log analysis in the event of a breach
					message += " (ADMIN)";
				}
				log.info(message);

				return new JsonRepresentation(answer);

			} else {

				// count failed logins
				int attempts = user.getLoginAttempts();
				attempts++;
				user.setLoginAttempts(attempts);

				// too many, lock user
				if (attempts >= MAX_LOGIN_ATTEMPTS) {
					log.warn(String.format("Authorization failure: User account %s (ID %s - email %s) locked due to too many failed logins", user.getUsername(), user.getId(), user.getMail()));
					user.setLockedUntil(new Date(System.currentTimeMillis() + (LOCKING_TIME_MIN * 60 * 1000)));
				}
				dao.update(user);

				log.warn(String.format("Authorization failure: Invalid password for username: %s", username));
				return new JSONAnswer("Login Failed! Wrong Username or Password.", false);
			}
		} else {
			log.warn(String.format("Authorization failure: unknown username: %s", username));
			return new JSONAnswer("Login Failed! Wrong Username or Password.", false);
		}
	}

}
