package cloudgene.mapred.api.v2.users;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import cloudgene.mapred.TestApplication;
import cloudgene.mapred.util.TestMailServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import jakarta.inject.Inject;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RegisterUserTest {

	@Inject
	TestApplication application;

	@BeforeAll
	protected void setUp() throws Exception {
		TestMailServer.getInstance().start();
	}

	@Test
	public void testWithCorrectData() {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		// register new user
		Map<String, String> form = new HashMap<String, String>();
		form.put("username", "usernameunique");
		form.put("full-name", "full name");
		form.put("mail", "test-uniquent@test.com");
		form.put("new-password", "Password27");
		form.put("confirm-new-password", "Password27");

		RestAssured.given().formParams(form).when().post("/api/v2/users/register").then().statusCode(200).and()
				.body("success", equalTo(true)).and().body("message", equalTo("User sucessfully created."));

		// check if one mail was sent to user
		assertEquals(mailsBefore + 1, mailServer.getReceivedEmailSize());

		mailsBefore = mailServer.getReceivedEmailSize();

		// test with same username
		RestAssured.given().formParams(form).when().post("/api/v2/users/register").then().statusCode(200).and()
				.body("success", equalTo(false)).and().body("message", equalTo("Username already exists."));
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());

		// test with same email but different username
		form.put("username", "usernameuniqueunique");

		RestAssured.given().formParams(form).when().post("/api/v2/users/register").then().statusCode(200).and()
				.body("success", equalTo(false)).and().body("message", equalTo("E-Mail is already registered."));

		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());

	}

	@Test
	public void testWithEmptyUsername() {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Map<String, String> form = new HashMap<String, String>();
		form.put("username", "");
		form.put("full-name", "full name");
		form.put("mail", "test@test.com");
		form.put("new-password", "Password27");
		form.put("confirm-new-password", "Password27");

		RestAssured.given().formParams(form).when().post("/api/v2/users/register").then().statusCode(200).and()
				.body("success", equalTo(false)).and().body("message", containsString("username is required"));

		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());

	}

	@Test
	public void testWithWrongUsername() {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Map<String, String> form = new HashMap<String, String>();
		form.put("username", "username-");
		form.put("full-name", "full name");
		form.put("mail", "test@test.com");
		form.put("new-password", "Password27");
		form.put("confirm-new-password", "Password27");

		RestAssured.given().formParams(form).when().post("/api/v2/users/register").then().statusCode(200).and()
				.body("success", equalTo(false)).and().body("message", containsString("Your username is not valid"));
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());

		form = new HashMap<String, String>();
		form.put("username", "username.");
		form.put("full-name", "full name");
		form.put("mail", "test@test.com");
		form.put("new-password", "Password27");
		form.put("confirm-new-password", "Password27");

		RestAssured.given().formParams(form).when().post("/api/v2/users/register").then().statusCode(200).and()
				.body("success", equalTo(false)).and().body("message", containsString("Your username is not valid"));
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());

		form = new HashMap<String, String>();
		form.put("username", "username#");
		form.put("full-name", "full name");
		form.put("mail", "test@test.com");
		form.put("new-password", "Password27");
		form.put("confirm-new-password", "Password27");

		RestAssured.given().formParams(form).when().post("/api/v2/users/register").then().statusCode(200).and()
				.body("success", equalTo(false)).and().body("message", containsString("Your username is not valid"));
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());

	}

	@Test
	public void testWithShortUsername() {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Map<String, String> form = new HashMap<String, String>();
		form.put("username", "abc");
		form.put("full-name", "full name");
		form.put("mail", "test@test.com");
		form.put("new-password", "Password27");
		form.put("confirm-new-password", "Password27");

		RestAssured.given().formParams(form).when().post("/api/v2/users/register").then().statusCode(200).and()
				.body("success", equalTo(false)).and().body("message", containsString("username must contain at least"));
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());

	}

	@Test
	public void testWithEmptyName() {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Map<String, String> form = new HashMap<String, String>();
		form.put("username", "abcde");
		form.put("full-name", "");
		form.put("mail", "test@test.com");
		form.put("new-password", "Password27");
		form.put("confirm-new-password", "Password27");

		RestAssured.given().formParams(form).when().post("/api/v2/users/register").then().statusCode(200).and()
				.body("success", equalTo(false)).and().body("message", containsString("full name is required"));
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());

	}

	@Test
	public void testWithEmptyMail() {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Map<String, String> form = new HashMap<String, String>();
		form.put("username", "abcde");
		form.put("full-name", "abcdefgh abcgd");
		form.put("mail", "");
		form.put("new-password", "Password27");
		form.put("confirm-new-password", "Password27");

		RestAssured.given().formParams(form).when().post("/api/v2/users/register").then().statusCode(200).and()
				.body("success", equalTo(false)).and().body("message", containsString("E-Mail is required."));
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());

	}

	@Test
	public void testWithWrongMail() {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Map<String, String> form = new HashMap<String, String>();
		form.put("username", "abcde");
		form.put("full-name", "abcdefgh abcgd");
		form.put("mail", "test");
		form.put("new-password", "Password27");
		form.put("confirm-new-password", "Password27");

		RestAssured.given().formParams(form).when().post("/api/v2/users/register").then().statusCode(200).and()
				.body("success", equalTo(false)).and().body("message", containsString("a valid mail address"));
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());
		
	}

	@Test
	public void testWithWrongConfirmPassword() {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Map<String, String> form = new HashMap<String, String>();
		form.put("username", "abcde");
		form.put("full-name", "abcdefgh abcgd");
		form.put("mail", "test@test.com");
		form.put("new-password", "password");
		form.put("confirm-new-password", "password1");

		RestAssured.given().formParams(form).when().post("/api/v2/users/register").then().statusCode(200).and()
				.body("success", equalTo(false)).and().body("message", equalTo("Please check your passwords."));
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());

	}

	@Test
	public void testWithWrongPasswordLength() {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Map<String, String> form = new HashMap<String, String>();
		form.put("username", "abcde");
		form.put("full-name", "abcdefgh abcgd");
		form.put("mail", "test@test.com");
		form.put("new-password", "pass");
		form.put("confirm-new-password", "pass");

		RestAssured.given().formParams(form).when().post("/api/v2/users/register").then().statusCode(200).and()
				.body("success", equalTo(false)).and().body("message", containsString("contain at least"));
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());

	}

	@Test
	public void testWithPasswordWithMissingUppercase() {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Map<String, String> form = new HashMap<String, String>();
		form.put("username", "abcde");
		form.put("full-name", "abcdefgh abcgd");
		form.put("mail", "test@test.com");
		form.put("new-password", "passwordword27");
		form.put("confirm-new-password", "passwordword27");

		RestAssured.given().formParams(form).when().post("/api/v2/users/register").then().statusCode(200).and()
				.body("success", equalTo(false)).and().body("message", containsString("least one uppercase"));
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());

	}

	@Test
	public void testWithPasswordWithMissingLowercase() {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Map<String, String> form = new HashMap<String, String>();
		form.put("username", "abcde");
		form.put("full-name", "abcdefgh abcgd");
		form.put("mail", "test@test.com");
		form.put("new-password", "PASSWORD2727");
		form.put("confirm-new-password", "PASSWORD2727");

		RestAssured.given().formParams(form).when().post("/api/v2/users/register").then().statusCode(200).and()
				.body("success", equalTo(false)).and().body("message", containsString("least one lowercase"));
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());

	}

	@Test
	public void testWithPasswordWithMissingNumber() {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Map<String, String> form = new HashMap<String, String>();
		form.put("username", "abcde");
		form.put("full-name", "abcdefgh abcgd");
		form.put("mail", "test@test.com");
		form.put("new-password", "PASSWORDpassword");
		form.put("confirm-new-password", "PASSWORDpassword");

		RestAssured.given().formParams(form).when().post("/api/v2/users/register").then().statusCode(200).and()
				.body("success", equalTo(false)).and().body("message", containsString("least one number"));
		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());

	}

}
