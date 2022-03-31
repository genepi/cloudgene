package cloudgene.mapred.api.v2.users;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.restlet.data.Form;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import cloudgene.mapred.TestApplication;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.server.auth.DatabaseAuthenticationProvider;
import cloudgene.mapred.util.CloudgeneClient;
import cloudgene.mapred.util.HashUtil;
import genepi.db.Database;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LoginUserTest {

	@Inject
	TestApplication application;

	@Inject
	CloudgeneClient client;

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
		testUser3.setPassword(HashUtil.hashPassword("lockedpasssord"));
		userDao.insert(testUser3);

	}

	@Test
	public void testActivatedUser() throws Exception {

		// login should work
		ClientResource resource = client.createClientResource("/login");
		Form form = new Form();
		form.set("username", "testuser1");
		form.set("password", "test1");

		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals("testuser1", object.get("username"));
		assertTrue(object.has("access_token"));
		resource.release();
	}

	@Test
	public void testWrongPasswordAndLocking() throws JSONException, IOException {

		UserDao dao = new UserDao(application.getDatabase());

		// try with correct password
		for (int i = 1; i < 10; i++) {

			ClientResource resource = client.createClientResource("/login");
			Form form = new Form();
			form.set("username", "lockeduser");
			form.set("password", "lockedpasssord");

			resource.post(form);
			assertEquals(200, resource.getStatus().getCode());

			JSONObject object = new JSONObject(resource.getResponseEntity().getText());
			assertEquals("lockeduser", object.get("username"));
			assertTrue(object.has("access_token"));

			// check login attempts are the same
			int newLoginAttempts = dao.findByUsername("lockeduser").getLoginAttempts();
			assertEquals(0, newLoginAttempts);
			resource.release();
		}

		int oldLoginAttempts = dao.findByUsername("lockeduser").getLoginAttempts();

		// login should work xx times
		for (int i = 1; i < DatabaseAuthenticationProvider.MAX_LOGIN_ATTEMMPTS + 10; i++) {
			ClientResource resource = client.createClientResource("/login");
			Form form = new Form();
			form.set("username", "lockeduser");
			form.set("password", "wrong-password");
			try {
				resource.post(form);
			} catch (ResourceException e) {

			}
			assertEquals(401, resource.getStatus().getCode());
			JSONObject object = new JSONObject(resource.getResponseEntity().getText());

			int newLoginAttempts = dao.findByUsername("lockeduser").getLoginAttempts();

			if (i <= DatabaseAuthenticationProvider.MAX_LOGIN_ATTEMMPTS) {
				// check login counter
				assertEquals(oldLoginAttempts + 1, newLoginAttempts);
				oldLoginAttempts = newLoginAttempts;
				// check error messages
				assertEquals("Login Failed! Wrong Username or Password.", object.getString("message"));
			} else {
				assertEquals("The user account is locked for " + DatabaseAuthenticationProvider.LOCKING_TIME_MIN
						+ " minutes. Too many failed logins.", object.getString("message"));
			}
			resource.release();
		}

		// try with correct password
		for (int i = 1; i < 10; i++) {
			ClientResource resource = client.createClientResource("/login");
			Form form = new Form();
			form.set("username", "lockeduser");
			form.set("password", "lockedpasssord");

			try {
				resource.post(form);
			} catch (ResourceException e) {

			}
			assertEquals(401, resource.getStatus().getCode());
			JSONObject object = new JSONObject(resource.getResponseEntity().getText());
			assertEquals("The user account is locked for " + DatabaseAuthenticationProvider.LOCKING_TIME_MIN
					+ " minutes. Too many failed logins.", object.getString("message"));
			resource.release();
		}

		// update locked until to now
		User user = dao.findByUsername("lockeduser");
		user.setLockedUntil(new Date());
		dao.update(user);

		// try with correct password
		for (int i = 1; i < 10; i++) {

			ClientResource resource = client.createClientResource("/login");
			Form form = new Form();
			form.set("username", "lockeduser");
			form.set("password", "lockedpasssord");

			resource.post(form);
			assertEquals(200, resource.getStatus().getCode());
			JSONObject object = new JSONObject(resource.getResponseEntity().getText());
			assertEquals("lockeduser", object.getString("username"));
			assertTrue(object.has("access_token"));

			// check login attempts are the same
			int newLoginAttempts = dao.findByUsername("lockeduser").getLoginAttempts();
			assertEquals(0, newLoginAttempts);
			resource.release();
		}

	}

	@Test
	public void testInActivateUser() throws JSONException, IOException {

		// login should work
		ClientResource resource = client.createClientResource("/login");
		Form form = new Form();
		form.set("username", "testuser2");
		form.set("password", "test2");
		try {
			resource.post(form);
		} catch (ResourceException e) {

		}

		assertEquals(401, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals("Login Failed! User account is not activated.", object.getString("message"));
		resource.release();
	}

}
