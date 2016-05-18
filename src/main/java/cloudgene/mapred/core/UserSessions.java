package cloudgene.mapred.core;

import net.minidev.json.JSONObject;

import org.restlet.Request;

public class UserSessions {

	public static String KEY = "my-scret-key";

	public static final String COOKIE_NAME = "cloudgene-token";

	public UserSessions() {
	}

	public String loginUser(User user) {

		JSONObject playload = new JSONObject();
		playload.put("username", user.getUsername());
		playload.put("name", user.getFullName());
		playload.put("mail", user.getMail());

		String token = JWT.generate(playload, KEY);

		return token;
	}

	public String getUserByPayload(JSONObject payload) {
		return payload.get("username").toString();
	}

	public String getUserByRequest(Request request) {
		String token = request.getCookies().getFirstValue(COOKIE_NAME);

		// check cookie
		if (token != null) {

			JSONObject payload = JWT.validate(token, KEY);

			if (payload != null) {

				return getUserByPayload(payload);

			} else {

				// invalid access-token

				return null;
			}

		} else {

			// check auth header

			JSONObject payload = JWT.validate(request, KEY);

			if (payload != null) {

				return getUserByPayload(payload);

			} else {

				// invalid access-token

				return null;

			}
		}

	}

}
