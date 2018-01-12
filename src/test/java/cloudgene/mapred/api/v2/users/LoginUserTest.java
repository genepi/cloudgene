package cloudgene.mapred.api.v2.users;

import java.io.IOException;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.resource.ClientResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.junit.JobsApiTestCase;
import cloudgene.mapred.util.junit.TestServer;
import genepi.db.Database;

public class LoginUserTest extends JobsApiTestCase {

	@Override
	protected void setUp() throws Exception {
		TestServer.getInstance().start();

		// insert two dummy users
		Database database = TestServer.getInstance().getDatabase();
		UserDao userDao = new UserDao(database);

		User testUser1 = new User();
		testUser1.setUsername("testuser1");
		testUser1.setFullName("test1");
		testUser1.setMail("testuser1@test.com");
		testUser1.setRoles(new String[] { "User" });
		testUser1.setActive(true);
		testUser1.setActivationCode("");
		testUser1.setPassword(HashUtil.getMD5("test1"));
		userDao.insert(testUser1);

		User testUser2 = new User();
		testUser2.setUsername("testuser2");
		testUser2.setFullName("test2");
		testUser2.setMail("testuser2@test.com");
		testUser2.setRoles(new String[] { "User" });
		testUser2.setActive(false);
		testUser2.setActivationCode("some-activation-code");
		testUser2.setPassword(HashUtil.getMD5("test2"));
		userDao.insert(testUser2);

		User testUser3 = new User();
		testUser3.setUsername("lockeduser");
		testUser3.setFullName("test1");
		testUser3.setMail("testuser1@test.com");
		testUser3.setRoles(new String[] { "User" });
		testUser3.setActive(true);
		testUser3.setActivationCode("");
		testUser3.setPassword(HashUtil.getMD5("lockedpasssord"));
		userDao.insert(testUser3);

	}

	public void testActivatedUser() throws JSONException, IOException {

		// login should work
		ClientResource resource = createClientResource("/login");
		Form form = new Form();
		form.set("loginUsername", "testuser1");
		form.set("loginPassword", "test1");

		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals("Login successfull.", object.getString("message"));
		assertEquals(true, object.get("success"));
		assertEquals(1, resource.getResponse().getCookieSettings().size());
		resource.release();
	}

	public void testWrongPasswordAndLocking() throws JSONException, IOException {

		UserDao dao = new UserDao(TestServer.getInstance().getDatabase());

		// try with correct password
		for (int i = 1; i < 10; i++) {

			ClientResource resource = createClientResource("/login");
			Form form = new Form();
			form.set("loginUsername", "lockeduser");
			form.set("loginPassword", "lockedpasssord");

			resource.post(form);
			assertEquals(200, resource.getStatus().getCode());
			JSONObject object = new JSONObject(resource.getResponseEntity().getText());
			assertEquals("Login successfull.", object.getString("message"));
			assertEquals(true, object.get("success"));
			assertEquals(1, resource.getResponse().getCookieSettings().size());

			// check login attempts are the same
			int newLoginAttempts = dao.findByUsername("lockeduser").getLoginAttempts();
			assertEquals(0, newLoginAttempts);
			resource.release();
		}

		int oldLoginAttempts = dao.findByUsername("lockeduser").getLoginAttempts();

		// login should work xx times
		for (int i = 1; i < LoginUser.MAX_LOGIN_ATTEMMPTS + 10; i++) {
			ClientResource resource = createClientResource("/login");
			Form form = new Form();
			form.set("loginUsername", "lockeduser");
			form.set("loginPassword", "wrong-password");

			resource.post(form);
			assertEquals(200, resource.getStatus().getCode());
			JSONObject object = new JSONObject(resource.getResponseEntity().getText());

			int newLoginAttempts = dao.findByUsername("lockeduser").getLoginAttempts();

			if (i <= LoginUser.MAX_LOGIN_ATTEMMPTS) {
				// check login counter
				assertEquals(oldLoginAttempts + 1, newLoginAttempts);
				oldLoginAttempts = newLoginAttempts;
				// check error messages
				assertEquals("Login Failed! Wrong Username or Password.", object.getString("message"));
				assertEquals(false, object.get("success"));
				assertEquals(0, resource.getResponse().getCookieSettings().size());
			} else {
				assertEquals("The user account is locked for " + LoginUser.LOCKING_TIME_MIN
						+ " minutes. Too many failed logins.", object.getString("message"));
				assertEquals(false, object.get("success"));
				assertEquals(0, resource.getResponse().getCookieSettings().size());
			}
			resource.release();
		}

		// try with correct password
		for (int i = 1; i < 10; i++) {
			ClientResource resource = createClientResource("/login");
			Form form = new Form();
			form.set("loginUsername", "lockeduser");
			form.set("loginPassword", "lockedpasssord");

			resource.post(form);
			assertEquals(200, resource.getStatus().getCode());
			JSONObject object = new JSONObject(resource.getResponseEntity().getText());

			assertEquals("The user account is locked for " + LoginUser.LOCKING_TIME_MIN
					+ " minutes. Too many failed logins.", object.getString("message"));
			assertEquals(false, object.get("success"));
			assertEquals(0, resource.getResponse().getCookieSettings().size());
			resource.release();
		}

		// update locked until to now
		User user = dao.findByUsername("lockeduser");
		user.setLockedUntil(new Date());
		dao.update(user);

		// try with correct password
		for (int i = 1; i < 10; i++) {

			ClientResource resource = createClientResource("/login");
			Form form = new Form();
			form.set("loginUsername", "lockeduser");
			form.set("loginPassword", "lockedpasssord");

			resource.post(form);
			assertEquals(200, resource.getStatus().getCode());
			JSONObject object = new JSONObject(resource.getResponseEntity().getText());
			assertEquals("Login successfull.", object.getString("message"));
			assertEquals(true, object.get("success"));
			assertEquals(1, resource.getResponse().getCookieSettings().size());

			// check login attempts are the same
			int newLoginAttempts = dao.findByUsername("lockeduser").getLoginAttempts();
			assertEquals(0, newLoginAttempts);
			resource.release();
		}

	}

	public void testInActivateUser() throws JSONException, IOException {

		// login should work
		ClientResource resource = createClientResource("/login");
		Form form = new Form();
		form.set("loginUsername", "testuser2");
		form.set("loginPassword", "test2");

		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals("Login Failed! User account is not activated.", object.getString("message"));
		assertEquals(false, object.get("success"));
		assertEquals(0, resource.getResponse().getCookieSettings().size());
		resource.release();
	}

}
