package cloudgene.mapred.core;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWT;

import cloudgene.mapred.database.UserDao;
import genepi.db.Database;

public class ApiToken {

	public static long TOKEN_LIFETIME_MS = 24 * 60 * 60 * 1000;

	public static long TOKEN_LIFETIME_API_MS = 30L * 24L * 60L * 60L * 1000L;

	public static String create(User user, String secretKey) {

		Map<String, Object> playload = new HashMap<String, Object>();
		playload.put("username", user.getUsername());
		playload.put("name", user.getFullName());
		playload.put("mail", user.getMail());
		playload.put("api", true);
		String token = generate(playload, secretKey, TOKEN_LIFETIME_API_MS);

		return token;
	}
	
	protected static String generate( Map<String, Object> jsonObject, String key, long lifetime) {

		// add valid-until to payload
		jsonObject
				.put("expire", System.currentTimeMillis() + lifetime);

		// Create JWS header with HS256 algorithm
		JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256).
                contentType("text/plain").build();

		// Create JWS payload
		Payload payload = new Payload(jsonObject);

		// Create JWS object
		JWSObject jwsObject = new JWSObject(header, payload);

		try {
			JWSSigner signer = new MACSigner(key);
			jwsObject.sign(signer);
			return jwsObject.serialize();
		} catch (JOSEException e) {
			e.printStackTrace();
			return null;
		}

	}
	
	public static JSONObject verify(String token, String secretKey, Database database) {

		if (token.isEmpty()) {
			JSONObject result = new JSONObject();
			result.put("valid", false);
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
					payload.put("message", "Invalid Usern mae in API Token.");
				} else {

					Date expire = new Date((Long) payload.get("expire"));

					if (((Long) payload.get("expire")) > System.currentTimeMillis()) {

						// check if api key is on users whitelist
						if (user.getApiToken().equals(token)) {
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
