package cloudgene.mapred.api.v2.users;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.resource.ClientResource;

import com.dumbster.smtp.SmtpMessage;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.junit.JobsApiTestCase;
import cloudgene.mapred.util.junit.TestMailServer;
import cloudgene.mapred.util.junit.TestServer;
import genepi.db.Database;

public class ResetPasswordTest extends JobsApiTestCase {

	@Override
	protected void setUp() throws Exception {
		TestServer.getInstance().start();
		TestMailServer.getInstance().start();

		// insert two dummy users
		Database database = TestServer.getInstance().getDatabase();
		UserDao userDao = new UserDao(database);

		User testUser1 = new User();
		testUser1.setUsername("testreset");
		testUser1.setFullName("test1");
		testUser1.setMail("testuser1@test.com");
		testUser1.setRoles(new String[] { "User" });
		testUser1.setActive(true);
		testUser1.setActivationCode("");
		testUser1.setPassword(HashUtil.getMD5("oldpassword"));
		userDao.insert(testUser1);

		User testUse2 = new User();
		testUse2.setUsername("testreset2");
		testUse2.setFullName("test1");
		testUse2.setMail("testuser1@test.com");
		testUse2.setRoles(new String[] { "User" });
		testUse2.setActive(false);
		testUse2.setActivationCode("fdsfdsfsdfsdfsd");
		testUse2.setPassword(HashUtil.getMD5("oldpassword"));
		userDao.insert(testUse2);

	}

	public void testWithWrongName() throws JSONException, IOException {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Form form = new Form();
		form.set("username", "unknown-user-wrong");

		// register user
		ClientResource resource = createClientResource("/api/v2/users/reset");
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertEquals("We couldn't find an account with that username or email.", object.get("message").toString());
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());
		resource.release();
	}

	public void testWithInActiveUser() throws JSONException, IOException {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Form form = new Form();
		form.set("username", "testreset2");

		// register user
		ClientResource resource = createClientResource("/api/v2/users/reset");
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertEquals("Account is not activated.", object.get("message").toString());
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());
		resource.release();
	}

	public void testWithWrongEMail() throws JSONException, IOException {
		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Form form = new Form();
		form.set("username", "wrong@e-mail.com");

		// register user
		ClientResource resource = createClientResource("/api/v2/users/reset");
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertEquals("We couldn't find an account with that username or email.", object.get("message").toString());
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());
		resource.release();
	}

	public void testWithSpecial() throws JSONException, IOException {
		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Form form = new Form();
		form.set("username", "%");

		// register user
		ClientResource resource = createClientResource("/api/v2/users/reset");
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertEquals("We couldn't find an account with that username or email.", object.get("message").toString());
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());
		resource.release();
	}

	public void testResetPassword() throws JSONException, IOException {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		// form data
		Form form = new Form();
		form.set("username", "testreset");

		// register user
		ClientResource resource = createClientResource("/api/v2/users/reset");
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
		Database database = TestServer.getInstance().getDatabase();
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
