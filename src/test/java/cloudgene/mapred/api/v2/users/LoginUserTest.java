package cloudgene.mapred.api.v2.users;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import cloudgene.mapred.TestApplication;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.server.auth.DatabaseAuthenticationProvider;
import cloudgene.mapred.util.HashUtil;
import genepi.db.Database;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import jakarta.inject.Inject;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LoginUserTest {

	@Inject
	TestApplication application;

	@BeforeAll
	public void setUp() throws Exception {

		// insert two dummy users
		Database database = application.getDatabase();
		UserDao userDao = new UserDao(database);

		User testUser1 = new User();
		testUser1.setUsername("testuser1");
		testUser1.setFullName("test1");
		testUser1.setMail("testuser1@test.com");
		testUser1.setRoles(new String[] { "User" });
		testUser1.setActive(true);
		testUser1.setActivationCode("");
		testUser1.setPassword(HashUtil.hashPassword("test1"));
		userDao.insert(testUser1);

		User testUser2 = new User();
		testUser2.setUsername("testuser2");
		testUser2.setFullName("test2");
		testUser2.setMail("testuser2@test.com");
		testUser2.setRoles(new String[] { "User" });
		testUser2.setActive(false);
		testUser2.setActivationCode("some-activation-code");
		testUser2.setPassword(HashUtil.hashPassword("test2"));
		userDao.insert(testUser2);

		User testUser3 = new User();
		testUser3.setUsername("lockeduser");
		testUser3.setFullName("test1");
		testUser3.setMail("testuser1@test.com");
		testUser3.setRoles(new String[] { "User" });
		testUser3.setActive(true);
		testUser3.setActivationCode("");
		testUser3.setPassword(HashUtil.hashPassword("lockedpassword"));
		userDao.insert(testUser3);

	}

	@Test
	public void testActivatedUser() throws Exception {

		Map<String, String> form = new HashMap<String, String>();
		form.put("username", "testuser1");
		form.put("password", "test1");

		RestAssured.given().formParams(form).when().post("/login").then().statusCode(200).and()
				.body("username", equalTo("testuser1")).and().body("access_token", not(emptyString()));

	}

	@Test
	public void testWrongPasswordAndLocking() {

		UserDao dao = new UserDao(application.getDatabase());

		// try with correct password
		for (int i = 1; i < 10; i++) {

			Map<String, String> form = new HashMap<String, String>();
			form.put("username", "lockeduser");
			form.put("password", "lockedpassword");

			RestAssured.given().formParams(form).when().post("/login").then().statusCode(200).and()
					.body("username", equalTo("lockeduser")).and().body("access_token", not(emptyString()));

			// check login attempts are the same
			int newLoginAttempts = dao.findByUsername("lockeduser").getLoginAttempts();
			assertEquals(0, newLoginAttempts);

		}

		int oldLoginAttempts = dao.findByUsername("lockeduser").getLoginAttempts();

		// login should work xx times
		for (int i = 1; i < DatabaseAuthenticationProvider.MAX_LOGIN_ATTEMMPTS + 10; i++) {

			Map<String, String> form = new HashMap<String, String>();
			form.put("username", "lockeduser");
			form.put("password", "wrong-password");

			Response response = RestAssured.given().formParams(form).when().post("/login").thenReturn();
			response.then().statusCode(401);

			int newLoginAttempts = dao.findByUsername("lockeduser").getLoginAttempts();

			if (i <= DatabaseAuthenticationProvider.MAX_LOGIN_ATTEMMPTS) {
				// check login counter
				assertEquals(oldLoginAttempts + 1, newLoginAttempts);
				oldLoginAttempts = newLoginAttempts;

				// check error messages
				response.then().body("message", equalTo("Login Failed! Wrong Username or Password."));
			} else {
				String expectedMessage = "The user account is locked for "
						+ DatabaseAuthenticationProvider.LOCKING_TIME_MIN + " minutes. Too many failed logins.";
				response.then().body("message", equalTo(expectedMessage));
			}

		}

		// try with correct password
		for (int i = 1; i < 10; i++) {

			Map<String, String> form = new HashMap<String, String>();
			form.put("username", "lockeduser");
			form.put("password", "lockedpassword");

			String expectedMessage = "The user account is locked for " + DatabaseAuthenticationProvider.LOCKING_TIME_MIN
					+ " minutes. Too many failed logins.";
			RestAssured.given().formParams(form).when().post("/login").then().statusCode(401).and().body("message",
					equalTo(expectedMessage));

		}

		// update locked until to now
		User user = dao.findByUsername("lockeduser");
		user.setLockedUntil(new Date());
		dao.update(user);

		// try with correct password
		for (int i = 1; i < 10; i++) {

			Map<String, String> form = new HashMap<String, String>();
			form.put("username", "lockeduser");
			form.put("password", "lockedpassword");

			RestAssured.given().formParams(form).when().post("/login").then().statusCode(200).and()
					.body("username", equalTo("lockeduser")).and().body("access_token", not(emptyString()));

			// check login attempts are the same
			int newLoginAttempts = dao.findByUsername("lockeduser").getLoginAttempts();
			assertEquals(0, newLoginAttempts);
		}

	}

	@Test
	public void testInActivateUser() {

		Map<String, String> form = new HashMap<String, String>();
		form.put("username", "testuser2");
		form.put("password", "test2");

		RestAssured.given().formParams(form).when().post("/login").then().statusCode(401).and()
				.body("message", equalTo("Login Failed! User account is not activated.")).and()
				.body("access_token", IsNull.nullValue());

	}

}
