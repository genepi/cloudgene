package cloudgene.mapred.core;

import java.util.Date;
import java.util.Map;

import org.json.JSONObject;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;

import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.database.util.Database;

public class ApiTokenVerifier {
	
	public static JSONObject verify(String token, String secretKey, Database database) {

		if (token.isEmpty()) {
			JSONObject result = new JSONObject();
			result.put("valid", false);
			result.put("message", "No token provided");
			return result;
		}

		try {

			JWSVerifier verifier = new MACVerifier(secretKey);
			JWSObject jwsObject = JWSObject.parse(token);

			if (jwsObject.verify(verifier)) {
				// read valid-until and check
				Map<String, Object> payload = jwsObject.getPayload().toJSONObject();

				User user = getUser(database, payload);
				if (user == null) {
					payload.put("valid", false);
					payload.put("message", "Invalid Username in API Token.");
				} else {

					Date expire = new Date((Long) payload.get("expire"));

					if (((Long) payload.get("expire")) > System.currentTimeMillis()) {

						// check if api key is on users whitelist
						if (user.getApiToken().equals(payload.get(JWTUtil.ATTRIBUTE_API_HASH))) {
							payload.put("valid", true);
							payload.put("message", "API Token was created by " + user.getUsername()
									+ " and is valid until " + expire + ".");
						} else {
							payload.put("valid", false);
							payload.put("message", "API Token revoked by user.");
						}
					} else {
						payload.put("valid", false);
						payload.put("message",
								"API Token was created by " + user.getUsername() + " and expired on " + expire + ".");
					}
				}

				return new JSONObject(payload);

			} else {
				JSONObject result = new JSONObject();
				result.put("valid", false);
				result.put("message", "API Token is not valid.");
				return result;
			}

		} catch (Exception e1) {
			JSONObject result = new JSONObject();
			result.put("valid", false);
			result.put("error", "Unkown error.");
			return result;
		}

	}

	public static User getUser(Database database, Map<String, Object> payload) {
		String username = payload.get("username").toString();
		if (username != null) {
			UserDao userDao = new UserDao(database);
			return userDao.findByUsername(username);
		} else {
			return null;
		}
	}
}
