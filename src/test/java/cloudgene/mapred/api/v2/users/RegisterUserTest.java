package cloudgene.mapred.api.v2.users;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.resource.ClientResource;

import cloudgene.mapred.api.v2.JobsApiTestCase;
import cloudgene.mapred.util.junit.TestServer;

public class RegisterUserTest extends JobsApiTestCase {

	@Override
	protected void setUp() throws Exception {
		TestServer.getInstance().start();
	}

	public void testWithCorrectData() throws JSONException, IOException {
		// form data

		Form form = new Form();
		form.set("username", "username");
		form.set("full-name", "full name");
		form.set("mail", "test@test.com");
		form.set("new-password", "Password27");
		form.set("confirm-new-password", "Password27");

		// register user
		ClientResource resource = createClientResource("/api/v2/users/register");

		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		//no email set in testcases!
		assertTrue(object.get("message").toString().contains("mail could not be sent"));

	}

	public void testWithEmptyUsername() throws JSONException, IOException {
		// form data

		Form form = new Form();
		form.set("username", "");
		form.set("full-name", "full name");
		form.set("mail", "test@test.com");
		form.set("new-password", "Password27");
		form.set("confirm-new-password", "Password27");

		// register user
		ClientResource resource = createClientResource("/api/v2/users/register");
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertTrue(object.get("message").toString().contains("username is required"));

	}

	public void testWithWrongUsername() throws JSONException, IOException {
		// form data

		Form form = new Form();
		form.set("username", "username-");
		form.set("full-name", "full name");
		form.set("mail", "test@test.com");
		form.set("new-password", "Password27");
		form.set("confirm-new-password", "Password27");

		// register user
		ClientResource resource = createClientResource("/api/v2/users/register");
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertTrue(object.get("message").toString().contains("Your username is not valid"));

	}

	public void testWithShortUsername() throws JSONException, IOException {
		// form data

		Form form = new Form();
		form.set("username", "abc");
		form.set("full-name", "full name");
		form.set("mail", "test@test.com");
		form.set("new-password", "Password27");
		form.set("confirm-new-password", "Password27");

		// register user
		ClientResource resource = createClientResource("/api/v2/users/register");
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertTrue(object.get("message").toString().contains("username must contain at least"));

	}

	public void testWithEmptyName() throws JSONException, IOException {
		// form data

		Form form = new Form();
		form.set("username", "abcde");
		form.set("full-name", "");
		form.set("mail", "test@test.com");
		form.set("new-password", "Password27");
		form.set("confirm-new-password", "Password27");

		// register user
		ClientResource resource = createClientResource("/api/v2/users/register");
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertTrue(object.get("message").toString().contains("full name is required"));

	}

	public void testWithEmptyMail() throws JSONException, IOException {
		// form data

		Form form = new Form();
		form.set("username", "abcde");
		form.set("full-name", "abcdefgh abcgd");
		form.set("mail", "");
		form.set("new-password", "Password27");
		form.set("confirm-new-password", "Password27");

		// register user
		ClientResource resource = createClientResource("/api/v2/users/register");
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertTrue(object.get("message").toString().contains("E-Mail is required."));

	}

	public void testWithWrongMail() throws JSONException, IOException {
		// form data

		Form form = new Form();
		form.set("username", "abcde");
		form.set("full-name", "abcdefgh abcgd");
		form.set("mail", "test");
		form.set("new-password", "Password27");
		form.set("confirm-new-password", "Password27");

		// register user
		ClientResource resource = createClientResource("/api/v2/users/register");
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertTrue(object.get("message").toString().contains("a valid mail address"));

	}

	public void testWithWrongConfirmPassword() throws JSONException, IOException {
		// form data

		Form form = new Form();
		form.set("username", "abcde");
		form.set("full-name", "abcdefgh abcgd");
		form.set("mail", "test@test.com");
		form.set("new-password", "password");
		form.set("confirm-new-password", "password1");

		// register user
		ClientResource resource = createClientResource("/api/v2/users/register");
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertTrue(object.get("message").toString().contains("check your passwords"));

	}

	public void testWithWrongPasswordLength() throws JSONException, IOException {
		// form data

		Form form = new Form();
		form.set("username", "abcde");
		form.set("full-name", "abcdefgh abcgd");
		form.set("mail", "test@test.com");
		form.set("new-password", "pass");
		form.set("confirm-new-password", "pass");

		// register user
		ClientResource resource = createClientResource("/api/v2/users/register");
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertTrue(object.get("message").toString().contains("contain at least"));

	}

	public void testWithPasswordWithMissingUppercase() throws JSONException, IOException {
		// form data

		Form form = new Form();
		form.set("username", "abcde");
		form.set("full-name", "abcdefgh abcgd");
		form.set("mail", "test@test.com");
		form.set("new-password", "passwordword27");
		form.set("confirm-new-password", "passwordword27");

		// register user
		ClientResource resource = createClientResource("/api/v2/users/register");
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertTrue(object.get("message").toString().contains("least one uppercase"));

	}

	public void testWithPasswordWithMissingLowercase() throws JSONException, IOException {
		// form data

		Form form = new Form();
		form.set("username", "abcde");
		form.set("full-name", "abcdefgh abcgd");
		form.set("mail", "test@test.com");
		form.set("new-password", "PASSWORD2727");
		form.set("confirm-new-password", "PASSWORD2727");

		// register user
		ClientResource resource = createClientResource("/api/v2/users/register");
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertTrue(object.get("message").toString().contains("least one lowercase"));

	}

	public void testWithPasswordWithMissingNumber() throws JSONException, IOException {
		// form data

		Form form = new Form();
		form.set("username", "abcde");
		form.set("full-name", "abcdefgh abcgd");
		form.set("mail", "test@test.com");
		form.set("new-password", "PASSWORDpassword");
		form.set("confirm-new-password", "PASSWORDpassword");

		// register user
		ClientResource resource = createClientResource("/api/v2/users/register");
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertTrue(object.get("message").toString().contains("least one number"));

	}

}
