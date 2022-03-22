package cloudgene.mapred.api.v2.users;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.restlet.data.Form;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.dumbster.smtp.SmtpMessage;

import cloudgene.mapred.TestApplication;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.util.CloudgeneClient;
import cloudgene.mapred.util.TestMailServer;
import genepi.db.Database;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ActivateUserTest {

	@Inject
	TestApplication application;

	@Inject
	CloudgeneClient client;

	@BeforeAll
	protected void setUp() throws Exception {
		TestMailServer.getInstance().start();
	}

	@Test
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
		ClientResource resource = client.createClientResource("/api/v2/users/register");

		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(true, object.get("success"));
		// no email set in testcases!
		assertEquals("User sucessfully created.", object.get("message"));

		// check if one mail was sent to user
		assertEquals(mailsBefore + 1, mailServer.getReceivedEmailSize());

		// get activation key from database
		Database database = application.getDatabase();
		UserDao userDao = new UserDao(database);
		User user = userDao.findByUsername("usernameunique5");
		assertNotNull(user);

		SmtpMessage message = mailServer.getReceivedEmailAsList().get(mailsBefore);
		// check if correct key is in mail
		assertTrue(message.getBody().contains(user.getActivationCode()));
		resource.release();
		
		// login should not be possible
		resource = client.createClientResource("/login");
		form = new Form();
		form.set("username", "usernameunique5");
		form.set("password", "Password27");
		
		try {
			resource.post(form);
		} catch (ResourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertEquals(401, resource.getStatus().getCode());
		resource.release();

		//assertEquals(200, resource.getStatus().getCode());
		//object = new JSONObject(resource.getResponseEntity().getText());
		//assertEquals("Login Failed! User account is not activated.", object.getString("message"));
		//assertEquals(false, object.get("success"));
		//assertEquals(0, resource.getResponse().getCookieSettings().size());
		
		// activate user with wrong activation code
		resource = client.createClientResource("/users/activate/" + user.getUsername() + "/RANDOMACTIVATIONCODE");
		resource.get();
		object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals("Wrong activation code.", object.getString("message"));
		assertEquals(false, object.get("success"));
		resource.release();
		
		// activate user with wrong username
		resource = client.createClientResource("/users/activate/randomusername/" + user.getActivationCode());
		resource.get();
		object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals("Wrong username.", object.getString("message"));
		assertEquals(false, object.get("success"));
		resource.release();
		
		// login should not be possible
		resource = client.createClientResource("/login");
		form = new Form();
		form.set("username", "usernameunique5");
		form.set("password", "Password27");
		
		try {
			resource.post(form);
		} catch (ResourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertEquals(401, resource.getStatus().getCode());
		resource.release();

		//assertEquals(200, resource.getStatus().getCode());
		//object = new JSONObject(resource.getResponseEntity().getText());
		//assertEquals("Login Failed! User account is not activated.", object.getString("message"));
		//assertEquals(false, object.get("success"));
		//assertEquals(0, resource.getResponse().getCookieSettings().size());
		//resource.release();
		
		// activate user with correct data
		resource = client.createClientResource("/users/activate/" + user.getUsername() + "/" + user.getActivationCode());
		resource.get();
		object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals("User sucessfully activated.", object.getString("message"));
		assertEquals(true, object.get("success"));
		resource.release();
		
		// login should work
		resource = client.createClientResource("/login");
		form = new Form();
		form.set("username", "usernameunique5");
		form.set("password", "Password27");
		resource.post(form);
		
		assertEquals(200, resource.getStatus().getCode());
		object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals("usernameunique5", object.get("username"));
		//assertEquals("Login successfull.", object.getString("message"));
		//assertEquals(true, object.get("success"));
		//assertEquals(1, resource.getResponse().getCookieSettings().size());
		resource.release();
	}

}
