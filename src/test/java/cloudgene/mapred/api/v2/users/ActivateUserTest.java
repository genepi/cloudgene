package cloudgene.mapred.api.v2.users;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.dumbster.smtp.SmtpMessage;

import cloudgene.mapred.TestApplication;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.util.TestMailServer;
import genepi.db.Database;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import jakarta.inject.Inject;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ActivateUserTest {

	@Inject
	TestApplication application;

	@BeforeAll
	protected void setUp() throws Exception {
		TestMailServer.getInstance().start();
	}

	@Test
	public void testUserActivation() {
		
		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		// form data

		Map<String, String> form = new HashMap<String, String>();
		form.put("username", "usernameunique5");
		form.put("full-name", "full name");
		form.put("mail", "new.user@test.com");
		form.put("new-password", "Password27");
		form.put("confirm-new-password", "Password27");

		// register user
		RestAssured.given().formParams(form).when().post("/api/v2/users/register").then().statusCode(200).and()
				.body("success", equalTo(true)).and().body("message", equalTo("User sucessfully created."));

		// check if one mail was sent to user
		assertEquals(mailsBefore + 1, mailServer.getReceivedEmailSize());

		// get activation key from database
		Database database = application.getDatabase();
		UserDao userDao = new UserDao(database);
		User user = userDao.findByUsername("usernameunique5");
		assertNotNull(user);

		// check if correct key is in mail
		SmtpMessage message = mailServer.getReceivedEmailAsList().get(mailsBefore);
		assertTrue(message.getBody().contains(user.getActivationCode()));

		// login should not be possible
		form = new HashMap<String, String>();
		form.put("username", "usernameunique5");
		form.put("password", "Password27");

		RestAssured.given().formParams(form).when().post("/login").then().statusCode(401).and()
				.body("message", equalTo("Login Failed! User account is not activated."));

		// activate user with wrong activation code
		RestAssured.when().get("/users/activate/" + user.getUsername() + "/RANDOMACTIVATIONCODE").then().statusCode(200)
				.and().body("success", equalTo(false)).and().body("message", equalTo("Wrong activation code."));

		// activate user with wrong username
		RestAssured.when().get("/users/activate/randomusername/" + user.getActivationCode()).then().statusCode(200)
				.and().body("success", equalTo(false)).and().body("message", equalTo("Wrong username."));

		// login should not be possible after wrong activation attempts
		form = new HashMap<String, String>();
		form.put("username", "usernameunique5");
		form.put("password", "Password27");

		RestAssured.given().formParams(form).when().post("/login").then().statusCode(401).and().body("message",
				equalTo("Login Failed! User account is not activated."));

		// activate user with correct data
		RestAssured.when().get("/users/activate/" + user.getUsername() + "/" + user.getActivationCode()).then()
				.statusCode(200).and().body("success", equalTo(true)).and()
				.body("message", equalTo("User sucessfully activated."));

		// login should work
		form = new HashMap<String, String>();
		form.put("username", "usernameunique5");
		form.put("password", "Password27");

		RestAssured.given().formParams(form).when().post("/login").then().statusCode(200).and().and()
				.body("username", equalTo("usernameunique5")).and().body("access_token", notNullValue());

	}

}
