package cloudgene.mapred.util;

import org.springframework.security.crypto.bcrypt.BCrypt;

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

    /** Return BCrypt hash from input */
    public static String getBCrypt(String input) {
        return BCrypt.hashpw(input, BCrypt.gensalt());
    }

    /** Check if a provided candidate password is the same as an existing hash */
    public static boolean checkBCrypt(String candidate, String hash) {
        return BCrypt.checkpw(candidate, hash);
    }
}
