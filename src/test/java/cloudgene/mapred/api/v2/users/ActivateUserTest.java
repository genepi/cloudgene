package cloudgene.mapred.api.v2.users;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.resource.ClientResource;

import com.dumbster.smtp.SmtpMessage;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.util.junit.JobsApiTestCase;
import cloudgene.mapred.util.junit.TestMailServer;
import cloudgene.mapred.util.junit.TestServer;
import genepi.db.Database;

public class ActivateUserTest extends JobsApiTestCase {

	@Override
	protected void setUp() throws Exception {
		TestServer.getInstance().start();
		TestMailServer.getInstance().start();
	}

	public void testUserActivation() throws JSONException, IOException {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		// form data

		Form form = new Form();
		form.set("username", "usernameunique5");
		form.set("full-name", "full name");
		form.set("mail", "new.user@test.com");
		form.set("new-password", "Password27");
		form.set("confirm-new-password", "Password27");

		// register user
		ClientResource resource = createClientResource("/api/v2/users/register");

		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(true, object.get("success"));
		// no email set in testcases!
		assertEquals("User sucessfully created.", object.get("message"));

		// check if one mail was sent to user
		assertEquals(mailsBefore + 1, mailServer.getReceivedEmailSize());

		// get activation key from database
		Database database = TestServer.getInstance().getDatabase();
		UserDao userDao = new UserDao(database);
		User user = userDao.findByUsername("usernameunique5");
		assertNotNull(user);

		SmtpMessage message = mailServer.getReceivedEmailAsList().get(mailsBefore);
		// check if correct key is in mail
		assertTrue(message.getBody().contains(user.getActivationCode()));
		resource.release();
		
		// login should not be possible
		resource = createClientResource("/login");
		form = new Form();
		form.set("loginUsername", "usernameunique5");
		form.set("loginPassword", "Password27");
		resource.post(form);

		assertEquals(200, resource.getStatus().getCode());
		object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals("Login Failed! User account is not activated.", object.getString("message"));
		assertEquals(false, object.get("success"));
		assertEquals(0, resource.getResponse().getCookieSettings().size());
		resource.release();
		
		// activate user with wrong activation code
		resource = createClientResource("/users/activate/" + user.getUsername() + "/RANDOMACTIVATIONCODE");
		resource.get();
		object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals("Wrong activation code.", object.getString("message"));
		assertEquals(false, object.get("success"));
		resource.release();
		
		// activate user with wrong username
		resource = createClientResource("/users/activate/randomusername/" + user.getActivationCode());
		resource.get();
		object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals("Wrong username.", object.getString("message"));
		assertEquals(false, object.get("success"));
		resource.release();
		
		// login should not be possible
		resource = createClientResource("/login");
		form = new Form();
		form.set("loginUsername", "usernameunique5");
		form.set("loginPassword", "Password27");
		resource.post(form);

		assertEquals(200, resource.getStatus().getCode());
		object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals("Login Failed! User account is not activated.", object.getString("message"));
		assertEquals(false, object.get("success"));
		assertEquals(0, resource.getResponse().getCookieSettings().size());
		resource.release();
		
		// activate user with correct data
		resource = createClientResource("/users/activate/" + user.getUsername() + "/" + user.getActivationCode());
		resource.get();
		object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals("User sucessfully activated.", object.getString("message"));
		assertEquals(true, object.get("success"));
		resource.release();
		
		// login should work
		resource = createClientResource("/login");
		form = new Form();
		form.set("loginUsername", "usernameunique5");
		form.set("loginPassword", "Password27");
		resource.post(form);
		
		assertEquals(200, resource.getStatus().getCode());
		object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals("Login successfull.", object.getString("message"));
		assertEquals(true, object.get("success"));
		assertEquals(1, resource.getResponse().getCookieSettings().size());
		resource.release();
	}

}
