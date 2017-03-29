package cloudgene.mapred.core;

import net.minidev.json.JSONObject;

import org.restlet.Request;
import org.restlet.data.Parameter;
import org.restlet.util.Series;

public class JWTUtil {

	public static final String COOKIE_NAME = "cloudgene-token";

	public JWTUtil() {
	}

	public static String createCookieToken(User user, String secretKey, String csrfToken) {

		JSONObject playload = new JSONObject();
		playload.put("username", user.getUsername());
		playload.put("name", user.getFullName());
		playload.put("mail", user.getMail());
		playload.put("api", false);
		playload.put("csrf", csrfToken);

		String token = JWT.generate(playload, secretKey);

		return token;
	}

	public static String createApiToken(User user, String secretKey) {

		JSONObject playload = new JSONObject();
		playload.put("username", user.getUsername());
		playload.put("name", user.getFullName());
		playload.put("mail", user.getMail());
		playload.put("api", true);

		String token = JWT.generate(playload, secretKey);

		return token;
	}

	public static String getUser(JSONObject payload) {
		return payload.get("username").toString();
	}

	public static boolean isApiToken(JSONObject payload) {
		return (Boolean) payload.get("api");
	}

	public static String getUserByRequest(Request request, String secretKey, boolean checkCsrf) {
		String token = request.getCookies().getFirstValue(COOKIE_NAME);

		// check cookie
		if (token != null) {

			// csrf token is needed!

			Series<Parameter> headers = (Series) request.getAttributes().get("org.restlet.http.headers");

			String csrfToken = headers.getFirstValue("X-CSRF-Token");
			if (csrfToken == null) {
				// IE 8 bug
				csrfToken = headers.getFirstValue("x-csrf-token");
			}
			System.out.println("Token Request: " + csrfToken);

			// no csrf token available
			if (checkCsrf && csrfToken == null) {
				return null;
			}

			JSONObject payload = JWT.validate(token, secretKey);

			if (payload != null) {

				System.out.println("Token Request: " + csrfToken);
				System.out.println("Token Key: " + payload.get("csrf").toString());

				// check csrf token
				if (!isApiToken(payload) && (!checkCsrf || csrfToken.equals(payload.get("csrf").toString()))) {
					return getUser(payload);
				} else {
					// csrf token is invalid
					return null;
				}

			} else {

				// invalid access-token

				return null;
			}

		} else {

			// check auth header: no cookie and csrf token needed, but key has
			// to be an API
			// key

			JSONObject payload = JWT.validate(request, secretKey);

			if (payload != null) {

				// check if it is an api key
				if (isApiToken(payload)) {
					return getUser(payload);
				} else {
					return null;
				}

			} else {

				// invalid access-token

				return null;

			}
		}

	}

}
