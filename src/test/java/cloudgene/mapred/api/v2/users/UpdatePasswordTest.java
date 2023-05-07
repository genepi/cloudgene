package cloudgene.mapred.api.v2.users;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import cloudgene.mapred.TestApplication;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.TestMailServer;
import genepi.db.Database;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import jakarta.inject.Inject;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UpdatePasswordTest {

	@Inject
	TestApplication application;

	@BeforeAll
	protected void setUp() throws Exception {
		TestMailServer.getInstance().start();

		// insert two dummy users
		Database database = application.getDatabase();
		UserDao userDao = new UserDao(database);

		User testUser1 = new User();
		testUser1.setUsername("testupdate");
		testUser1.setFullName("test1");
		testUser1.setMail("testuser1@test.com");
		testUser1.setRoles(new String[] { "User" });
		testUser1.setActive(true);
		testUser1.setActivationCode("ACTIVATION-CODE-FROM-MAIL");
		testUser1.setPassword(HashUtil.hashPassword("oldpassword"));
		userDao.insert(testUser1);

		User testUse2 = new User();
		testUse2.setUsername("testupdate2");
		testUse2.setFullName("test1");
		testUse2.setMail("testuser1@test.com");
		testUse2.setRoles(new String[] { "User" });
		testUse2.setActive(false);
		testUse2.setActivationCode("ACTIVATION-CODE-FROM-MAIL");
		testUse2.setPassword(HashUtil.hashPassword("oldpassword"));
		userDao.insert(testUse2);

		User testUser3 = new User();
		testUser3.setUsername("testupdate3");
		testUser3.setFullName("test1");
		testUser3.setMail("testuser1@test.com");
		testUser3.setRoles(new String[] { "User" });
		testUser3.setActive(true);
		testUser3.setActivationCode("ACTIVATION-CODE-FROM-MAIL-3");
		testUser3.setPassword(HashUtil.hashPassword("oldpassword"));
		userDao.insert(testUser3);

	}

	@Test
	public void testWithCorrectActivationCode() {

		// try to update invalid password
		Map<String, String> form = new HashMap<String, String>();
		form.put("token", "ACTIVATION-CODE-FROM-MAIL-3");
		form.put("username", "testupdate3");
		form.put("new-password", "new-password9");
		form.put("confirm-new-password", "new-password9");

		RestAssured.given().formParams(form).when().post("/api/v2/users/update-password").then().statusCode(200).and()
				.body("success", equalTo(false)).and()
				.body("message", equalTo("Password must contain at least one uppercase letter (A-Z)!"));

		// try to update password
		form = new HashMap<String, String>();
		form.put("token", "ACTIVATION-CODE-FROM-MAIL-3");
		form.put("username", "testupdate3");
		form.put("new-password", "New-password9");
		form.put("confirm-new-password", "New-password9");

		RestAssured.given().formParams(form).when().post("/api/v2/users/update-password").then().statusCode(200).and()
				.body("success", equalTo(true)).and().body("message", equalTo("Password sucessfully updated."));

		// try login with old password
		form = new HashMap<String, String>();
		form.put("username", "testupdate3");
		form.put("password", "old-password");
		RestAssured.given().formParams(form).when().post("/login").then().statusCode(401).and()
				.body("message", equalTo("Login Failed! Wrong Username or Password."));

		// try login with new password
		form = new HashMap<String, String>();
		form.put("username", "testupdate3");
		form.put("password", "New-password9");
		RestAssured.given().formParams(form).when().post("/login").then().statusCode(200).and()
				.body("username", equalTo("testupdate3")).and().body("access_token", notNullValue());

	}

	@Test
	public void testWithWrongActivationCode() {

		Map<String, String> form = new HashMap<String, String>();
		form.put("token", "WRONG TOKEN");
		form.put("username", "testupdate");
		form.put("new-password", "Password27");
		form.put("confirm-new-password", "Password27");

		RestAssured.given().formParams(form).when().post("/api/v2/users/update-password").then().statusCode(200).and()
				.body("success", equalTo(false)).and()
				.body("message", equalTo("Your recovery request is invalid or expired."));

	}

	@Test
	public void testWithEmptyUsername() {

		Map<String, String> form = new HashMap<String, String>();
		form.put("token", "ACTIVATION-CODE-FROM-MAIL");
		form.put("new-password", "Password27");
		form.put("confirm-new-password", "Password27");

		RestAssured.given().formParams(form).when().post("/api/v2/users/update-password").then().statusCode(200).and()
				.body("success", equalTo(false)).and().body("message", equalTo("No username set."));

	}

	@Test
	public void testWithWrongUsername() {

		Map<String, String> form = new HashMap<String, String>();
		form.put("token", "ACTIVATION-CODE-FROM-MAIL");
		form.put("username", "wrong-username");
		form.put("new-password", "Password27");
		form.put("confirm-new-password", "Password27");

		RestAssured.given().formParams(form).when().post("/api/v2/users/update-password").then().statusCode(200).and()
				.body("success", equalTo(false)).and()
				.body("message", equalTo("We couldn't find an account with that username or email."));

	}

	@Test
	public void testWithInActiveUser() {

		Map<String, String> form = new HashMap<String, String>();
		form.put("token", "ACTIVATION-CODE-FROM-MAIL");
		form.put("username", "testupdate2");
		form.put("new-password", "Password27");
		form.put("confirm-new-password", "Password27");

		RestAssured.given().formParams(form).when().post("/api/v2/users/update-password").then().statusCode(200).and()
				.body("success", equalTo(false)).and().body("message", equalTo("Account is not activated."));

	}

}
