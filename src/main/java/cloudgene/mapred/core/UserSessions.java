package cloudgene.mapred.core;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.restlet.Request;

public class UserSessions {

	private static UserSessions instance;

	private Map<String, User> users;

	public static final String COOKIE_NAME = "emi_session_id";

	public static UserSessions getInstance() {
		if (instance == null) {
			instance = new UserSessions();
		}
		return instance;
	}

	private UserSessions() {
		users = new HashMap<String, User>();
	}

	public String loginUser(User user) {
		String token = generateToken();
		users.put(token, user);
		return token;
	}

	public void logoutUserByToken(String token) {
		users.remove(token);
	}

	public User getUserByToken(String token) {
		return users.get(token);
	}

	public User getUserByRequest(Request request) {
		String token = request.getCookies().getFirstValue(COOKIE_NAME);

		if (token != null) {
			return getUserByToken(token);
		} else {
			return null;
		}

	}

	private String generateToken() {
		// get current Time
		long currentTime = System.currentTimeMillis();
		// generate random number between 0 and 1000
		Random generator2 = new Random(currentTime);
		int randomIndex = generator2.nextInt(1000);
		// multiply two values
		String s = String.valueOf(currentTime * randomIndex);
		// calculate MD5 value
		MessageDigest m = null;
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		m.update(s.getBytes(), 0, s.length());
		String cookieValue = new BigInteger(1, m.digest()).toString(16);
		return cookieValue;
	}

}
