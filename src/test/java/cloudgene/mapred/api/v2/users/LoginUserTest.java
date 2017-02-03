package cloudgene.mapred.api.v2.users;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.resource.ClientResource;

import cloudgene.mapred.api.v2.JobsApiTestCase;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.util.HashUtil;
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
		testUser1.setRole("User");
		testUser1.setActive(true);
		testUser1.setActivationCode("");
		testUser1.setPassword(HashUtil.getMD5("test1"));
		userDao.insert(testUser1);

		User testUser2 = new User();
		testUser2.setUsername("testuser2");
		testUser2.setFullName("test2");
		testUser2.setMail("testuser2@test.com");
		testUser2.setRole("User");
		testUser2.setActive(false);
		testUser2.setActivationCode("some-activation-code");
		testUser2.setPassword(HashUtil.getMD5("test2"));
		userDao.insert(testUser2);

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
	}

	public void testWrongPassword() throws JSONException, IOException {

		// login should work
		ClientResource resource = createClientResource("/login");
		Form form = new Form();
		form.set("loginUsername", "testuser1");
		form.set("loginPassword", "wrong-password");

		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals("Login Failed! Wrong Username or Password.", object.getString("message"));
		assertEquals(false, object.get("success"));
		assertEquals(0, resource.getResponse().getCookieSettings().size());
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
		assertEquals("Login Failed! Wrong Username or Password.", object.getString("message"));
		assertEquals(false, object.get("success"));
		assertEquals(0, resource.getResponse().getCookieSettings().size());
	}

}
