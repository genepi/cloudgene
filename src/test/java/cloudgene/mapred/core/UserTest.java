package cloudgene.mapred.core;

import junit.framework.TestCase;

public class UserTest extends TestCase {

	public void testUsernameRules() {

		assertNotNull(User.checkUsername("user_name"));
		assertNotNull(User.checkUsername("user#name"));
		assertNotNull(User.checkUsername("user-name"));
		assertNotNull(User.checkUsername("user!name"));
		assertNotNull(User.checkUsername("_user name"));
		assertNotNull(User.checkUsername("user.name"));
		assertNotNull(User.checkUsername(",user,name"));

		assertNull(User.checkUsername("username"));
		assertNull(User.checkUsername("username27"));
		assertNull(User.checkUsername("usernameUsername"));
		assertNull(User.checkUsername("12username12"));
		assertNull(User.checkUsername("user12name"));
		assertNull(User.checkUsername("uSeRnAmE"));

	}

	public void testPasswordRules() {

		assertNotNull(User.checkPassword("password1", "password"));
		assertNotNull(User.checkPassword("pass", "pass"));
		assertNotNull(User.checkPassword("password", "password"));
		assertNotNull(User.checkPassword("PassworD", "PassworD"));
		assertNotNull(User.checkPassword("PassworDpassword", "PassworDpassword"));

		assertNull(User.checkPassword("PassworDpassword2", "PassworDpassword2"));

	}

	public void testMail() {
		assertNotNull(User.checkMail("user.name@host"));
		assertNotNull(User.checkMail("user.name@host."));
		assertNotNull(User.checkMail("user#.name@host.com"));
		assertNotNull(User.checkMail("user#.namehost.com"));
		assertNotNull(User.checkMail("user#.nameh@.com"));

		assertNull(User.checkMail("user.name@host.com"));
	}

}
