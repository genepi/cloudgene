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

import com.dumbster.smtp.SmtpMessage;

import cloudgene.mapred.TestApplication;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.util.CloudgeneClient;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.TestMailServer;
import genepi.db.Database;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ResetPasswordTest {

	@Inject
	TestApplication application;

	@Inject
	CloudgeneClient client;

	@BeforeAll
	protected void setUp() throws Exception {
		TestMailServer.getInstance().start();

		// insert two dummy users
		Database database = application.getDatabase();
		UserDao userDao = new UserDao(database);

		User testUser1 = new User();
		testUser1.setUsername("testreset");
		testUser1.setFullName("test1");
		testUser1.setMail("testuser1@test.com");
		testUser1.setRoles(new String[] { "User" });
		testUser1.setActive(true);
		testUser1.setActivationCode("");
		testUser1.setPassword(HashUtil.hashPassword("oldpassword"));
		userDao.insert(testUser1);

		User testUse2 = new User();
		testUse2.setUsername("testreset2");
		testUse2.setFullName("test1");
		testUse2.setMail("testuser1@test.com");
		testUse2.setRoles(new String[] { "User" });
		testUse2.setActive(false);
		testUse2.setActivationCode("fdsfdsfsdfsdfsd");
		testUse2.setPassword(HashUtil.hashPassword("oldpassword"));
		userDao.insert(testUse2);

	}

	@Test
	public void testWithWrongName() throws JSONException, IOException {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Form form = new Form();
		form.set("username", "unknown-user-wrong");

		// register user
		ClientResource resource = client.createClientResource("/api/v2/users/reset");
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertEquals("We couldn't find an account with that username or email.", object.get("message").toString());
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());
		resource.release();
	}

	@Test
	public void testWithInActiveUser() throws JSONException, IOException {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Form form = new Form();
		form.set("username", "testreset2");

		// register user
		ClientResource resource = client.createClientResource("/api/v2/users/reset");
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertEquals("Account is not activated.", object.get("message").toString());
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());
		resource.release();
	}

	@Test
	public void testWithWrongEMail() throws JSONException, IOException {
		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Form form = new Form();
		form.set("username", "wrong@e-mail.com");

		// register user
		ClientResource resource = client.createClientResource("/api/v2/users/reset");
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertEquals("We couldn't find an account with that username or email.", object.get("message").toString());
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());
		resource.release();
	}

	@Test
	public void testWithSpecial() throws JSONException, IOException {
		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Form form = new Form();
		form.set("username", "%");

		// register user
		ClientResource resource = client.createClientResource("/api/v2/users/reset");
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertEquals("We couldn't find an account with that username or email.", object.get("message").toString());
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());
		resource.release();
	}

	@Test
	public void testResetPassword() throws JSONException, IOException {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		// form data
		Form form = new Form();
		form.set("username", "testreset");

		// register user
		ClientResource resource = client.createClientResource("/api/v2/users/reset");
		// register user

		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), true);
		assertTrue(object.get("message").toString().contains("Email sent to"));
		assertEquals(mailsBefore + 1, mailServer.getReceivedEmailSize());

		// try it a second time (nervous user)
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), true);

		assertTrue(object.get("message").toString().contains("Email sent to"));
		assertEquals(mailsBefore + 2, mailServer.getReceivedEmailSize());

		// check correct activtion code is in mail
		// get activation key from database
		Database database = application.getDatabase();
		UserDao userDao = new UserDao(database);
		User user = userDao.findByUsername("testreset");
		assertNotNull(user);

		SmtpMessage message1 = mailServer.getReceivedEmailAsList().get(mailsBefore);
		// check if correct key is in mail1
		assertTrue(message1.getBody().contains(user.getActivationCode()));

		SmtpMessage message2 = mailServer.getReceivedEmailAsList().get(mailsBefore + 1);
		// check if correct key is in mail2
		assertTrue(message2.getBody().contains(user.getActivationCode()));
		resource.release();
	}

}
