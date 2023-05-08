package cloudgene.mapred.api.v2.admin;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import cloudgene.mapred.TestApplication;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.util.CloudgeneClientRestAssured;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.TestMailServer;
import genepi.db.Database;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import jakarta.inject.Inject;

@MicronautTest()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ChangeGroupTest {

	@Inject
	TestApplication application;

	@Inject
	CloudgeneClientRestAssured client;

	@BeforeAll
	protected void setUp() throws Exception {
		TestMailServer.getInstance().start();

		// insert two dummy users
		Database database = application.getDatabase();
		UserDao userDao = new UserDao(database);

		User testUser3 = new User();
		testUser3.setUsername("username-group-test");
		testUser3.setFullName("test1");
		testUser3.setMail("testuser1@test.com");
		testUser3.setRoles(new String[] { "User" });
		testUser3.setActive(true);
		testUser3.setPassword(HashUtil.hashPassword("oldpassword"));
		userDao.insert(testUser3);

	}

	@Test
	public void testWithWrongCredentials() {

		Header accessToken = client.login("username-group-test", "oldpassword");

		Database database = application.getDatabase();
		UserDao userDao = new UserDao(database);

		User oldUser = userDao.findByUsername("username-group-test");

		RestAssured.given().header(accessToken).and().formParam("username", "username-group-test").and()
				.formParam("role", "user,newgroup,test").when().post("/api/v2/admin/users/changegroup").then()
				.statusCode(403);

		User newUser = userDao.findByUsername("username-group-test");
		// check if user in database is still the same
		assertEquals(String.join(User.ROLE_SEPARATOR, oldUser.getRoles()),
				String.join(User.ROLE_SEPARATOR, newUser.getRoles()));

	}

	@Test
	public void testWithAdminCredentials() {

		Database database = application.getDatabase();
		UserDao userDao = new UserDao(database);

		User oldUser = userDao.findByUsername("username-group-test");

		// check permissions
		for (String role : oldUser.getRoles()) {
			assertTrue(oldUser.hasRole(role));
		}
		assertFalse(oldUser.hasRole("newgroup"));
		assertFalse(oldUser.hasRole("test"));
		assertFalse(oldUser.hasRole("secret-group"));
		assertFalse(oldUser.isAdmin());

		Header accessToken = client.login("admin", "admin1978");

		// update group
		RestAssured.given().header(accessToken).and().formParam("username", "username-group-test").and()
				.formParam("role", "user,newgroup,test").when().post("/api/v2/admin/users/changegroup").then()
				.statusCode(200).body("username", equalTo("username-group-test")).and()
				.body("role", equalTo("user,newgroup,test"));

		User newUser = userDao.findByUsername("username-group-test");
		// check update
		assertEquals("user,newgroup,test", String.join(User.ROLE_SEPARATOR, newUser.getRoles()));

		// check permissions
		assertTrue(newUser.hasRole("user"));
		assertTrue(newUser.hasRole("newgroup"));
		assertTrue(newUser.hasRole("test"));
		assertFalse(newUser.hasRole("secret-group"));
		assertFalse(newUser.isAdmin());

		// revert changes
		newUser.setRoles(oldUser.getRoles());
		userDao.update(newUser);

	}

}
