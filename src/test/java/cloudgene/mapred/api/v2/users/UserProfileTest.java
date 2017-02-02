package cloudgene.mapred.api.v2.users;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.resource.ClientResource;

import cloudgene.mapred.api.v2.JobsApiTestCase;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.junit.TestServer;
import genepi.db.Database;

public class UserProfileTest extends JobsApiTestCase {

	@Override
	protected void setUp() throws Exception {
		TestServer.getInstance().start();

		// insert two dummy users
		Database database = TestServer.getInstance().getDatabase();
		UserDao userDao = new UserDao(database);

		User testUser1 = new User();
		testUser1.setUsername("test1");
		testUser1.setFullName("test1");
		testUser1.setMail("test1@test.com");
		testUser1.setRole("User");
		testUser1.setActive(true);
		testUser1.setActivationCode("");
		testUser1.setPassword(HashUtil.getMD5("Test1Password"));
		userDao.insert(testUser1);

		User testUser2 = new User();
		testUser2.setUsername("test2");
		testUser2.setFullName("test2");
		testUser2.setMail("test2@test.com");
		testUser2.setRole("User");
		testUser2.setActive(true);
		testUser2.setActivationCode("");
		testUser2.setPassword(HashUtil.getMD5("Test2Password"));
		userDao.insert(testUser2);

	}

	public void testWithWrongCredentials() throws JSONException, IOException {

		// login as user test1
		CookieSetting cookie = getCookieForUser("test1", "Test1Password");

		// try to update password for test2
		ClientResource resource = createClientResource("/api/v2/users/me/profile");
		Form form = new Form();
		form.set("username", "test2");
		form.set("full-name", "test2 test1");
		form.set("mail", "test1@test.com");
		form.set("new-password", "Password27");
		form.set("confirm-new-password", "Password27");
		resource.getCookies().add(cookie);

		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertTrue(object.get("message").toString().contains("not allowed to change"));
	}
	
	public void testWithWrongConfirmPassword() throws JSONException, IOException {

		// login as user test1
		CookieSetting cookie = getCookieForUser("test1", "Test1Password");

		ClientResource resource = createClientResource("/api/v2/users/me/profile");
		Form form = new Form();

		// try to update with wrong password
		form.set("username", "test1");
		form.set("full-name", "test1 new");
		form.set("mail", "test1@test.com");
		form.set("new-password", "aaa");
		form.set("confirm-new-password", "abbb");
		resource.getCookies().add(cookie);

		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertTrue(object.get("message").toString().contains("check your passwords"));
	}

	public void testWithPasswordWithMissingLowercase() throws JSONException, IOException {

		// login as user test1
		CookieSetting cookie = getCookieForUser("test1", "Test1Password");

		ClientResource resource = createClientResource("/api/v2/users/me/profile");
		Form form = new Form();
		// try to update with wrong password
		form.set("username", "test1");
		form.set("full-name", "test1 new");
		form.set("mail", "test1@test.com");
		form.set("new-password", "PASSWORD2727");
		form.set("confirm-new-password", "PASSWORD2727");
		resource.getCookies().add(cookie);

		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertTrue(object.get("message").toString().contains("least one lowercase"));

	}

	public void testWithPasswordWithMissingNumber() throws JSONException, IOException {

		// login as user test1
		CookieSetting cookie = getCookieForUser("test1", "Test1Password");

		ClientResource resource = createClientResource("/api/v2/users/me/profile");
		Form form = new Form();

		// try to update with wrong password
		form.set("username", "test1");
		form.set("full-name", "test1 new");
		form.set("mail", "test1@test.com");
		form.set("new-password", "PASSWORDpassword");
		form.set("confirm-new-password", "PASSWORDpassword");
		resource.getCookies().add(cookie);

		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertTrue(object.get("message").toString().contains("least one number"));
	}

	public void testWithPasswordWithMissingUppercase() throws JSONException, IOException {

		// login as user test1
		CookieSetting cookie = getCookieForUser("test1", "Test1Password");

		ClientResource resource = createClientResource("/api/v2/users/me/profile");
		Form form = new Form();

		// try to update with wrong password
		form.set("username", "test1");
		form.set("full-name", "test1 new");
		form.set("mail", "test1@test.com");
		form.set("new-password", "passwordword27");
		form.set("confirm-new-password", "passwordword27");
		resource.getCookies().add(cookie);

		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertTrue(object.get("message").toString().contains("least one uppercase"));
	}

}
