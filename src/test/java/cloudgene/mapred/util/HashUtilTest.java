package cloudgene.mapred.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class HashUtilTest {

	@Test
	public void testBCryptHash() {
		String password = "P@assW0rd!Test";
		String passwordHash = HashUtil.hashPassword(password);
		assertTrue(HashUtil.checkPassword(password, passwordHash));
	}

	/*
	 * @Test public void testMD5Hash() { String password = "P@assW0rd!Test"; String
	 * passwordHash = HashUtil.getMD5(password);
	 * assertEquals("Generated MD5 hash should match",
	 * "801f65f0fb7f4417b2e2cc5490c88cc3", passwordHash); }
	 */
}
