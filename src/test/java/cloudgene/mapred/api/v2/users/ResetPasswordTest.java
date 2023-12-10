package cloudgene.mapred.api.v2.users;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
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
import cloudgene.mapred.database.util.Database;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.TestMailServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import jakarta.inject.Inject;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ResetPasswordTest {

	@Inject
	TestApplication application;

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
	public void testWithWrongName() {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Map<String, String> form = new HashMap<String, String>();
		form.put("username", "unknown-user-wrong");

		RestAssured.given().formParams(form).when().post("/api/v2/users/reset").then().statusCode(200).and()
				.body("success", equalTo(false)).and()
				.body("message", equalTo("We couldn't find an account with that username or email."));

		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());
	}

	@Test
	public void testWithInActiveUser() {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Map<String, String> form = new HashMap<String, String>();
		form.put("username", "testreset2");

		RestAssured.given().formParams(form).when().post("/api/v2/users/reset").then().statusCode(200).and()
				.body("success", equalTo(false)).and().body("message", equalTo("Account is not activated."));

		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());

	}

	@Test
	public void testWithWrongEMail() {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Map<String, String> form = new HashMap<String, String>();
		form.put("username", "wrong@e-mail.com");

		RestAssured.given().formParams(form).when().post("/api/v2/users/reset").then().statusCode(200).and()
				.body("success", equalTo(false)).and()
				.body("message", equalTo("We couldn't find an account with that username or email."));

		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());

	}

	@Test
	public void testWithSpecial() {
		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Map<String, String> form = new HashMap<String, String>();
		form.put("username", "%");

		RestAssured.given().formParams(form).when().post("/api/v2/users/reset").then().statusCode(200).and()
				.body("success", equalTo(false)).and()
				.body("message", equalTo("We couldn't find an account with that username or email."));

		assertEquals(mailsBefore, mailServer.getReceivedEmailSize());

	}

	@Test
	public void testResetPassword() {

		TestMailServer mailServer = TestMailServer.getInstance();
		int mailsBefore = mailServer.getReceivedEmailSize();

		Map<String, String> form = new HashMap<String, String>();
		form.put("username", "testreset");

		// rest password and check if mail was sent
		RestAssured.given().formParams(form).when().post("/api/v2/users/reset").then().statusCode(200).and()
				.body("success", equalTo(true)).and().body("message", containsString("We sent you an email"));

		assertEquals(mailsBefore + 1, mailServer.getReceivedEmailSize());

		// try it a second time (nervous user)
		RestAssured.given().formParams(form).when().post("/api/v2/users/reset").then().statusCode(200).and()
				.body("success", equalTo(true)).and().body("message", containsString("We sent you an email"));

		assertEquals(mailsBefore + 2, mailServer.getReceivedEmailSize());

		// get activation key from database and check if key was reused in mail2
		Database database = application.getDatabase();
		UserDao userDao = new UserDao(database);
		User user = userDao.findByUsername("testreset");
		assertNotNull(user);

		// check if correct key is in mail1
		SmtpMessage message1 = mailServer.getReceivedEmailAsList().get(mailsBefore);
		assertTrue(message1.getBody().contains(user.getActivationCode()));

		// check if correct key is in mail2
		SmtpMessage message2 = mailServer.getReceivedEmailAsList().get(mailsBefore + 1);
		assertTrue(message2.getBody().contains(user.getActivationCode()));

	}

}
