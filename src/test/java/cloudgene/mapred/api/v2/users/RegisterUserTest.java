package cloudgene.mapred.api.v2.users;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.restlet.data.Form;
import org.restlet.resource.ClientResource;

import cloudgene.mapred.TestApplication;
import cloudgene.mapred.util.CloudgeneClient;
import cloudgene.mapred.util.TestMailServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RegisterUserTest {

	@Inject
	TestApplication application;

	@Inject
	CloudgeneClient client;

	@BeforeAll
	protected void setUp() throws Exception {
		TestMailServer.getInstance().start();
	}

	@Test
	public void testWithCorrectData() throws JSONException, IOException {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		// form data

		Form form = new Form();
		form.set("username", "usernameunique");
		form.set("full-name", "full name");
		form.set("mail", "test-uniquent@test.com");
		form.set("new-password", "Password27");
		form.set("confirm-new-password", "Password27");

		// register user
		ClientResource resource = client.createClientResource("/api/v2/users/register");

		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), true);
		// no email set in testcases!
		assertEquals("User sucessfully created.", object.get("message"));

		// check if one mail was sent to user
		assertEquals(mailsBefore + 1, mailServer.getReceivedEmailSize());

		mailsBefore = mailServer.getReceivedEmailSize();

		// test with same username
		form = new Form();
		form.set("username", "usernameunique");
		form.set("full-name", "full name");
		form.set("mail", "test-uniquent@test.com");
		form.set("new-password", "Password27");
		form.set("confirm-new-password", "Password27");

		// register user
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		// no email set in testcases!
		assertEquals("Username already exists.", object.get("message"));
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());

		// test with same email but different username
		form = new Form();
		form.set("username", "usernameuniqueunique");
		form.set("full-name", "full name");
		form.set("mail", "test-uniquent@test.com");
		form.set("new-password", "Password27");
		form.set("confirm-new-password", "Password27");

		// register user
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		// no email set in testcases!
		assertEquals("E-Mail is already registered.", object.get("message"));
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());
		resource.release();
	}

	@Test
	public void testWithEmptyUsername() throws JSONException, IOException {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Form form = new Form();
		form.set("username", "");
		form.set("full-name", "full name");
		form.set("mail", "test@test.com");
		form.set("new-password", "Password27");
		form.set("confirm-new-password", "Password27");

		// register user
		ClientResource resource = client.createClientResource("/api/v2/users/register");
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertTrue(object.get("message").toString().contains("username is required"));
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());
		resource.release();

	}

	@Test
	public void testWithWrongUsername() throws JSONException, IOException {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Form form = new Form();
		form.set("username", "username-");
		form.set("full-name", "full name");
		form.set("mail", "test@test.com");
		form.set("new-password", "Password27");
		form.set("confirm-new-password", "Password27");

		// register user
		ClientResource resource = client.createClientResource("/api/v2/users/register");
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertTrue(object.get("message").toString().contains("Your username is not valid"));
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());

		form = new Form();
		form.set("username", "username.");
		form.set("full-name", "full name");
		form.set("mail", "test@test.com");
		form.set("new-password", "Password27");
		form.set("confirm-new-password", "Password27");

		// register user
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertTrue(object.get("message").toString().contains("Your username is not valid"));
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());

		form = new Form();
		form.set("username", "username#");
		form.set("full-name", "full name");
		form.set("mail", "test@test.com");
		form.set("new-password", "Password27");
		form.set("confirm-new-password", "Password27");

		// register user
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertTrue(object.get("message").toString().contains("Your username is not valid"));
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());
		resource.release();
	}

	@Test
	public void testWithShortUsername() throws JSONException, IOException {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Form form = new Form();
		form.set("username", "abc");
		form.set("full-name", "full name");
		form.set("mail", "test@test.com");
		form.set("new-password", "Password27");
		form.set("confirm-new-password", "Password27");

		// register user
		ClientResource resource = client.createClientResource("/api/v2/users/register");
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertTrue(object.get("message").toString().contains("username must contain at least"));
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());
		resource.release();
	}

	@Test
	public void testWithEmptyName() throws JSONException, IOException {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Form form = new Form();
		form.set("username", "abcde");
		form.set("full-name", "");
		form.set("mail", "test@test.com");
		form.set("new-password", "Password27");
		form.set("confirm-new-password", "Password27");

		// register user
		ClientResource resource = client.createClientResource("/api/v2/users/register");
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertTrue(object.get("message").toString().contains("full name is required"));
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());
		resource.release();
	}

	@Test
	public void testWithEmptyMail() throws JSONException, IOException {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Form form = new Form();
		form.set("username", "abcde");
		form.set("full-name", "abcdefgh abcgd");
		form.set("mail", "");
		form.set("new-password", "Password27");
		form.set("confirm-new-password", "Password27");

		// register user
		ClientResource resource = client.createClientResource("/api/v2/users/register");
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertTrue(object.get("message").toString().contains("E-Mail is required."));
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());
		resource.release();
	}

	@Test
	public void testWithWrongMail() throws JSONException, IOException {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Form form = new Form();
		form.set("username", "abcde");
		form.set("full-name", "abcdefgh abcgd");
		form.set("mail", "test");
		form.set("new-password", "Password27");
		form.set("confirm-new-password", "Password27");

		// register user
		ClientResource resource = client.createClientResource("/api/v2/users/register");
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertTrue(object.get("message").toString().contains("a valid mail address"));
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());
		resource.release();
	}

	@Test
	public void testWithWrongConfirmPassword() throws JSONException, IOException {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Form form = new Form();
		form.set("username", "abcde");
		form.set("full-name", "abcdefgh abcgd");
		form.set("mail", "test@test.com");
		form.set("new-password", "password");
		form.set("confirm-new-password", "password1");

		// register user
		ClientResource resource = client.createClientResource("/api/v2/users/register");
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertTrue(object.get("message").toString().contains("check your passwords"));
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());
		resource.release();
	}

	@Test
	public void testWithWrongPasswordLength() throws JSONException, IOException {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Form form = new Form();
		form.set("username", "abcde");
		form.set("full-name", "abcdefgh abcgd");
		form.set("mail", "test@test.com");
		form.set("new-password", "pass");
		form.set("confirm-new-password", "pass");

		// register user
		ClientResource resource = client.createClientResource("/api/v2/users/register");
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertTrue(object.get("message").toString().contains("contain at least"));
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());
		resource.release();
	}

	@Test
	public void testWithPasswordWithMissingUppercase() throws JSONException, IOException {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Form form = new Form();
		form.set("username", "abcde");
		form.set("full-name", "abcdefgh abcgd");
		form.set("mail", "test@test.com");
		form.set("new-password", "passwordword27");
		form.set("confirm-new-password", "passwordword27");

		// register user
		ClientResource resource = client.createClientResource("/api/v2/users/register");
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertTrue(object.get("message").toString().contains("least one uppercase"));
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());
		resource.release();
	}

	@Test
	public void testWithPasswordWithMissingLowercase() throws JSONException, IOException {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Form form = new Form();
		form.set("username", "abcde");
		form.set("full-name", "abcdefgh abcgd");
		form.set("mail", "test@test.com");
		form.set("new-password", "PASSWORD2727");
		form.set("confirm-new-password", "PASSWORD2727");

		// register user
		ClientResource resource = client.createClientResource("/api/v2/users/register");
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertTrue(object.get("message").toString().contains("least one lowercase"));
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());
		resource.release();
	}

	@Test
	public void testWithPasswordWithMissingNumber() throws JSONException, IOException {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Form form = new Form();
		form.set("username", "abcde");
		form.set("full-name", "abcdefgh abcgd");
		form.set("mail", "test@test.com");
		form.set("new-password", "PASSWORDpassword");
		form.set("confirm-new-password", "PASSWORDpassword");

		// register user
		ClientResource resource = client.createClientResource("/api/v2/users/register");
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertTrue(object.get("message").toString().contains("least one number"));
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());
		resource.release();
	}

}
