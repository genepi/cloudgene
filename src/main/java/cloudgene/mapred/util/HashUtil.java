package cloudgene.mapred.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.crypto.bcrypt.BCrypt;

import cloudgene.mapred.core.User;

public class HashUtil {

	public static String getActivationHash(User user) {
		return HashUtil.getSha256(System.currentTimeMillis() + "_" + Math.round(2000));
	}

	public static String getCsrfToken(User user) {
		return HashUtil.getSha256(System.currentTimeMillis() + "_" + Math.round(2000));
	}

	public static String getSha256(String name) {
		return DigestUtils.sha256Hex(name);
	}

	private static String getMD5(String pwd) {
		MessageDigest m = null;
		String result = "";
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		m.update(pwd.getBytes(), 0, pwd.length());
		result = new BigInteger(1, m.digest()).toString(16);
		return result;
	}

	/** Return BCrypt hash from input */
	public static String hashPassword(String input) {
		String hashed = getMD5(input);
		return BCrypt.hashpw(hashed, BCrypt.gensalt());
	}

	/** Check if a provided candidate password is the same as an existing hash */
	public static boolean checkPassword(String candidate, String hash) {
		String hashedCandidate = getMD5(candidate);
		return BCrypt.checkpw(hashedCandidate, hash);
	}
}
