package cloudgene.mapred.api.v2.users;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import cloudgene.mapred.server.services.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import cloudgene.mapred.TestApplication;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.database.util.Database;
import cloudgene.mapred.util.CloudgeneClientRestAssured;
import cloudgene.mapred.util.HashUtil;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import jakarta.inject.Inject;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserProfileTest {

	@Inject
	TestApplication application;

	@Inject
	CloudgeneClientRestAssured client;

	@BeforeAll
	protected void setUp() throws Exception {

		// insert two dummy users
		Database database = application.getDatabase();
		UserDao userDao = new UserDao(database);

		User testUser1 = new User();
		testUser1.setUsername("test1");
		testUser1.setFullName("test1");
		testUser1.setMail("test1@test.com");
		testUser1.setRoles(new String[] { "User" });
		testUser1.setActive(true);
		testUser1.setActivationCode("");
		testUser1.setPassword(HashUtil.hashPassword("Test1Password"));
		userDao.insert(testUser1);

		User testUser2 = new User();
		testUser2.setUsername("test2");
		testUser2.setFullName("test2");
		testUser2.setMail("test2@test.com");
		testUser2.setRoles(new String[] { "User" });
		testUser2.setActive(true);
		testUser2.setActivationCode("");
		testUser2.setPassword(HashUtil.hashPassword("Test2Password"));
		userDao.insert(testUser2);

	}

	@Test
	public void testGetWithWrongCredentials() {

		RestAssured.when().get("/api/v2/users/test1/profile").then().statusCode(401);

	}

	@Test
	public void testGetWithCorrectCredentials() {
		// login as user test1 and get profile. username is ignored, returns
		// always auth user's profile. just for better urls

		Header accessToken = client.login("test1", "Test1Password");

		RestAssured.given().header(accessToken).when().get("/api/v2/users/test1/profile").then().statusCode(200).and()
				.body("username", equalTo("test1")).and().body("mail", equalTo("test1@test.com"))
				.body("password", nullValue());

	}

	@Test
	public void testUpdateWithCorrectCredentials() {

		// login as user test1
		Header accessToken = client.login("test2", "Test2Password");

		// try to update password for test2
		Map<String, String> form = new HashMap<String, String>();
		form.put("username", "test2");
		form.put("full-name", "new full-name");
		form.put("mail", "test1@test.com");
		form.put("new-password", "new-Password27");
		form.put("confirm-new-password", "new-Password27");

		RestAssured.given().header(accessToken).and().formParams(form).when().post("/api/v2/users/test1/profile").then()
				.statusCode(200).and().body("success", equalTo(true)).and()
				.body("message", equalTo("User profile successfully updated."));

		// try login with old password
		form = new HashMap<String, String>();
		form.put("username", "test2");
		form.put("password", "old-Test2Password");
		RestAssured.given().formParams(form).when().post("/login").then().statusCode(401).and()
				.body("message", equalTo("Login Failed! Wrong Username or Password."));

		// try login with new password
		form = new HashMap<String, String>();
		form.put("username", "test2");
		form.put("password", "new-Password27");
		RestAssured.given().formParams(form).when().post("/login").then().statusCode(200).and()
				.body("username", equalTo("test2")).and().body("access_token", notNullValue());
	}

	@Test
	public void testUpdateWithWrongCredentials() {

		// login as user test1
		Header accessToken = client.login("test1", "Test1Password");

		// try to update password for test2
		Map<String, String> form = new HashMap<String, String>();
		form.put("username", "test2");
		form.put("full-name", "test2 test1");
		form.put("mail", "test1@test.com");
		form.put("new-password", "Password27");
		form.put("confirm-new-password", "Password27");

		RestAssured.given().header(accessToken).and().formParams(form).when().post("/api/v2/users/test1/profile").then()
				.statusCode(200).and().body("success", equalTo(false)).and()
				.body("message", containsString("not allowed to change"));

	}

	@Test
	public void testUpdateWithWrongConfirmPassword() {

		Header accessToken = client.login("test1", "Test1Password");

		Map<String, String> form = new HashMap<String, String>();
		form.put("username", "test1");
		form.put("full-name", "test1 new");
		form.put("mail", "test1@test.com");
		form.put("new-password", "aaa");
		form.put("confirm-new-password", "abbb");

		RestAssured.given().header(accessToken).and().formParams(form).when().post("/api/v2/users/test1/profile").then()
				.statusCode(200).and().body("success", equalTo(false)).and()
				.body("message", containsString("check your passwords"));
	}

	@Test
	public void testUpdatePasswordWithMissingLowercase() {

		Header accessToken = client.login("test1", "Test1Password");

		Map<String, String> form = new HashMap<String, String>();
		form.put("username", "test1");
		form.put("full-name", "test1 new");
		form.put("mail", "test1@test.com");
		form.put("new-password", "PASSWORD2727");
		form.put("confirm-new-password", "PASSWORD2727");

		RestAssured.given().header(accessToken).and().formParams(form).when().post("/api/v2/users/test1/profile").then()
				.statusCode(200).and().body("success", equalTo(false)).and()
				.body("message", containsString("least one lowercase"));

	}

	@Test
	public void testUpdatePasswordWithMissingNumber() {

		Header accessToken = client.login("test1", "Test1Password");

		Map<String, String> form = new HashMap<String, String>();
		form.put("username", "test1");
		form.put("full-name", "test1 new");
		form.put("mail", "test1@test.com");
		form.put("new-password", "PASSWORDpassword");
		form.put("confirm-new-password", "PASSWORDpassword");

		RestAssured.given().header(accessToken).and().formParams(form).when().post("/api/v2/users/test1/profile").then()
				.statusCode(200).and().body("success", equalTo(false)).and()
				.body("message", containsString("least one number"));

	}

	@Test
	public void testUpdatePasswordWithMissingUppercase() {

		Header accessToken = client.login("test1", "Test1Password");

		Map<String, String> form = new HashMap<String, String>();
		form.put("username", "test1");
		form.put("full-name", "test1 new");
		form.put("mail", "test1@test.com");
		form.put("new-password", "passwordword27");
		form.put("confirm-new-password", "passwordword27");

		RestAssured.given().header(accessToken).and().formParams(form).when().post("/api/v2/users/test1/profile").then()
				.statusCode(200).and().body("success", equalTo(false)).and()
				.body("message", containsString("least one uppercase"));

	}

	@Test
	public void testUpdateWithEmptyEmail() {

		Header accessToken = client.login("test1", "Test1Password");

		Map<String, String> form = new HashMap<String, String>();
		form.put("username", "test1");
		form.put("full-name", "test1 new");
		form.put("mail", "");

		RestAssured.given().header(accessToken).and().formParams(form).when().post("/api/v2/users/test1/profile").then().log().all()
				.statusCode(200).and().body("success", equalTo(false)).and()
				.body("message", containsString("E-Mail is required."));

	}

	@Test
	public void testDowngradeAndUpgradeAccount()  {

		application.getSettings().setEmailRequired(false);
		Header accessToken = client.login("test1", "Test1Password");

		// downgrade by removing email
		Map<String, String> form = new HashMap<String, String>();
		form.put("username", "test1");
		form.put("full-name", "test1 new");
		form.put("mail", "");

		RestAssured.given().header(accessToken).and().formParams(form).when().post("/api/v2/users/test1/profile").then()
				.statusCode(200).and().body("success", equalTo(true)).and()
				.body("message", containsString("downgraded"));

		//check role
		UserDao dao = new UserDao(application.getDatabase());
		User user = dao.findByUsername("test1");
		assertEquals(1, user.getRoles().length);
		assertEquals(UserService.DEFAULT_ANONYMOUS_ROLE, user.getRoles()[0]);

		//upgrade by adding email
		form = new HashMap<String, String>();
		form.put("username", "test1");
		form.put("full-name", "test1 new");
		form.put("mail", "test1@test.com");

		RestAssured.given().header(accessToken).and().formParams(form).when().post("/api/v2/users/test1/profile").then()
				.statusCode(200).and().body("success", equalTo(true)).and()
				.body("message", containsString("upgraded"));


		//check role
		user = dao.findByUsername("test1");
		assertEquals(1, user.getRoles().length);
		assertEquals(UserService.DEFAULT_ROLE, user.getRoles()[0]);

		application.getSettings().setEmailRequired(true);

	}

}
