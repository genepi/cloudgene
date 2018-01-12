package cloudgene.mapred.api.v2.users;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.resource.ClientResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.junit.JobsApiTestCase;
import cloudgene.mapred.util.junit.TestMailServer;
import cloudgene.mapred.util.junit.TestServer;
import genepi.db.Database;

public class UpdatePasswordTest extends JobsApiTestCase {

	@Override
	protected void setUp() throws Exception {
		TestServer.getInstance().start();
		TestMailServer.getInstance().start();

		// insert two dummy users
		Database database = TestServer.getInstance().getDatabase();
		UserDao userDao = new UserDao(database);

		User testUser1 = new User();
		testUser1.setUsername("testupdate");
		testUser1.setFullName("test1");
		testUser1.setMail("testuser1@test.com");
		testUser1.setRoles(new String[] { "User" });
		testUser1.setActive(true);
		testUser1.setActivationCode("ACTIVATION-CODE-FROM-MAIL");
		testUser1.setPassword(HashUtil.getMD5("oldpassword"));
		userDao.insert(testUser1);

		User testUse2 = new User();
		testUse2.setUsername("testupdate2");
		testUse2.setFullName("test1");
		testUse2.setMail("testuser1@test.com");
		testUse2.setRoles(new String[] { "User" });
		testUse2.setActive(false);
		testUse2.setActivationCode("ACTIVATION-CODE-FROM-MAIL");
		testUse2.setPassword(HashUtil.getMD5("oldpassword"));
		userDao.insert(testUse2);

		User testUser3 = new User();
		testUser3.setUsername("testupdate3");
		testUser3.setFullName("test1");
		testUser3.setMail("testuser1@test.com");
		testUser3.setRoles(new String[] { "User" });
		testUser3.setActive(true);
		testUser3.setActivationCode("ACTIVATION-CODE-FROM-MAIL-3");
		testUser3.setPassword(HashUtil.getMD5("oldpassword"));
		userDao.insert(testUser3);

	}

	public void testWithCorrectActivationCode() throws JSONException, IOException {

		// try to update invalid password
		ClientResource resource = createClientResource("/api/v2/users/update-password");
		Form form = new Form();
		form.set("token", "ACTIVATION-CODE-FROM-MAIL-3");
		form.set("username", "testupdate3");
		form.set("new-password", "new-password9");
		form.set("confirm-new-password", "new-password9");

		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(false, object.get("success"));
		assertEquals("Password must contain at least one uppercase letter (A-Z)!", object.get("message").toString());
		resource.release();

		// try to update password
		resource = createClientResource("/api/v2/users/update-password");
		form = new Form();
		form.set("token", "ACTIVATION-CODE-FROM-MAIL-3");
		form.set("username", "testupdate3");
		form.set("new-password", "New-password9");
		form.set("confirm-new-password", "New-password9");

		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(true, object.get("success"));
		assertEquals("Password sucessfully updated.", object.get("message").toString());
		resource.release();

		// try login with old password
		resource = createClientResource("/login");
		form = new Form();
		form.set("loginUsername", "testupdate3");
		form.set("loginPassword", "old-password");
		resource.post(form);

		assertEquals(200, resource.getStatus().getCode());
		object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals("Login Failed! Wrong Username or Password.", object.getString("message"));
		assertEquals(false, object.get("success"));
		assertEquals(0, resource.getResponse().getCookieSettings().size());
		resource.release();

		// try login with new password
		resource = createClientResource("/login");
		form = new Form();
		form.set("loginUsername", "testupdate3");
		form.set("loginPassword", "New-password9");
		resource.post(form);

		assertEquals(200, resource.getStatus().getCode());
		object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals("Login successfull.", object.getString("message"));
		assertEquals(true, object.get("success"));
		assertEquals(1, resource.getResponse().getCookieSettings().size());
		resource.release();
	}

	public void testWithWrongActivationCode() throws JSONException, IOException {

		// try to update password for test2
		ClientResource resource = createClientResource("/api/v2/users/update-password");
		Form form = new Form();
		form.set("token", "WRONG TOKEN");
		form.set("username", "testupdate");
		form.set("new-password", "Password27");
		form.set("confirm-new-password", "Password27");

		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertEquals("Your recovery request is invalid or expired.", object.get("message").toString());

	}

	public void testWithEmptyUsername() throws JSONException, IOException {

		// try to update password for test2
		ClientResource resource = createClientResource("/api/v2/users/update-password");
		Form form = new Form();
		form.set("token", "ACTIVATION-CODE-FROM-MAIL");
		form.set("new-password", "Password27");
		form.set("confirm-new-password", "Password27");

		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertEquals("No username set.", object.get("message").toString());
	}

	public void testWithWrongUsername() throws JSONException, IOException {

		// try to update password for test2
		ClientResource resource = createClientResource("/api/v2/users/update-password");
		Form form = new Form();
		form.set("token", "ACTIVATION-CODE-FROM-MAIL");
		form.set("username", "wrong-username");
		form.set("new-password", "Password27");
		form.set("confirm-new-password", "Password27");

		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertEquals("We couldn't find an account with that username.", object.get("message").toString());
	}

	public void testWithInActiveUser() throws JSONException, IOException {

		// try to update password for test2
		ClientResource resource = createClientResource("/api/v2/users/update-password");
		Form form = new Form();
		form.set("token", "ACTIVATION-CODE-FROM-MAIL");
		form.set("username", "testupdate2");
		form.set("new-password", "Password27");
		form.set("confirm-new-password", "Password27");

		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertEquals("Account is not activated.", object.get("message").toString());
	}

}
