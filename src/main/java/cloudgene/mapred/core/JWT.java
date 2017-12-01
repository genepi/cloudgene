package cloudgene.mapred.core;

import java.text.ParseException;

import org.restlet.Request;
import org.restlet.data.Parameter;
import org.restlet.util.Series;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;

import net.minidev.json.JSONObject;

public class JWT {

	// 1 day
	public static long TOKEN_LIFETIME_MS = 24 * 60 * 60 * 1000;

	public static String generate(JSONObject jsonObject, String key) {

		// add valid-until to payload
		jsonObject
				.put("expire", System.currentTimeMillis() + TOKEN_LIFETIME_MS);

		// Create JWS header with HS256 algorithm
		JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
		header.setContentType("text/plain");

		// Create JWS payload
		Payload payload = new Payload(jsonObject);

		// Create JWS object
		JWSObject jwsObject = new JWSObject(header, payload);

		JWSSigner signer = new MACSigner(key);
		try {
			jwsObject.sign(signer);
			return jwsObject.serialize();
		} catch (JOSEException e) {
			return null;
		}

	}

	public static JSONObject validate(Request request, String key) {

		Series<Parameter> headers = (Series) request.getAttributes().get(
				"org.restlet.http.headers");

		String token = headers.getFirstValue("X-Auth-Token");
		if (token != null) {
			return validate(token, key);
		}

		// IE 8 bug
		String token2 = headers.getFirstValue("x-auth-token");

		if (token2 != null) {
			return validate(token2, key);
		}

		// check if token is no in header, but in query
		String token3 = request.getResourceRef().getQueryAsForm()
				.getFirstValue("token");
		if (token3 != null) {
			return validate(token3, key);
		}

		// no token found
		return null;

	}

	public static JSONObject validate(String token, String key) {

		try {

			JWSVerifier verifier = new MACVerifier(key);
			JWSObject jwsObject = JWSObject.parse(token);

			if (jwsObject.verify(verifier)) {
				// read valid-until and check
				JSONObject payload = jwsObject.getPayload().toJSONObject();

				payload.put("request-token", token);
				
				if (((Long) payload.get("expire")) > System.currentTimeMillis()) {
					return payload;
				} else {
					return null;
				}

			}

		} catch (JOSEException e) {
			//e.printStackTrace();
			return null;

		} catch (ParseException e1) {
			//e1.printStackTrace();
			return null;

		} catch (Exception e1) {
			//e1.printStackTrace();
			return null;

		}

		return null;
	}

}
