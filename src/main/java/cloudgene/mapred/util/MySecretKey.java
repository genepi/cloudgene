package cloudgene.mapred.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class MySecretKey {
	public static final String key = "¾}k A¼(8¦§ð+o";

	public static void generateKey() {
		byte[] key = null;
		try {
		  KeyGenerator kgen = KeyGenerator.getInstance("AES");
		  kgen.init(128);
		  // mit "unlimited strength" security geht auch mehr:
		  // kgen.init(256);
		  SecretKey skey = kgen.generateKey();
		  key = skey.getEncoded();
		} catch (Exception e) {
		  // This sucks!
		}
	}
	
	public static String encrypt(String input)
			{
		System.out.println(input);
		byte[] encryptedBytes = null;
		try {
			byte[] bytes = input.getBytes("ISO-8859-1");
			SecretKeySpec specKey = new SecretKeySpec(key.getBytes("ISO-8859-1"),
					"AES");
			Cipher encryptCipher = Cipher.getInstance("AES/ECB/NoPadding");
			encryptCipher.init(Cipher.ENCRYPT_MODE, specKey);
			encryptedBytes = encryptCipher.doFinal(bytes);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		return Base64.encodeBase64String(encryptedBytes);
	}

	public static String decrypt(String input)
			 {
		byte[] encryptedBytes = null;
		try {
			byte[] bytes = Base64.decodeBase64(input);
			SecretKeySpec specKey = new SecretKeySpec(key.getBytes("ISO-8859-1"),
					"AES");
			Cipher encryptCipher = Cipher.getInstance("AES/ECB/NoPadding");
			encryptCipher.init(Cipher.DECRYPT_MODE, specKey);
			encryptedBytes = encryptCipher.doFinal(bytes);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new String(encryptedBytes);
	}

}
