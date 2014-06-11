package cloudgene.mapred.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {

	public static String getMD5(String pwd) {
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

	
}
