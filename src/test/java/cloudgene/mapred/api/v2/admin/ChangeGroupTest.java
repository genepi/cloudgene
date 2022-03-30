package cloudgene.mapred.api.v2.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
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
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.util.CloudgeneClient;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.LoginToken;
import cloudgene.mapred.util.TestMailServer;
import genepi.db.Database;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ChangeGroupTest {

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
	public void testWithWrongCredentials() throws JSONException, IOException {

		LoginToken token = client.login("username-group-test", "oldpassword");

		Database database = application.getDatabase();
		UserDao userDao = new UserDao(database);

		User oldUser = userDao.findByUsername("username-group-test");

		// try to update with no credentials
		ClientResource resource = client.createClientResource("/api/v2/admin/users/changegroup", token);
		Form form = new Form();
		form.set("username", "username-group-test");
		form.set("role", "user,newgroup,test");

		try {
			resource.post(form);
		} catch (Exception e) {
		}
		assertNotSame(200, resource.getStatus().getCode());
		resource.release();

		User newUser = userDao.findByUsername("username-group-test");
		// check if user in database is still the same
		assertEquals(String.join(User.ROLE_SEPARATOR, oldUser.getRoles()),
				String.join(User.ROLE_SEPARATOR, newUser.getRoles()));

	}

	@Test
	public void testWithAdminCredentials() throws JSONException, IOException {

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

		LoginToken token = client.login("admin", "admin1978");

		// try to update invalid password
		ClientResource resource = client.createClientResource("/api/v2/admin/users/changegroup", token);
		Form form = new Form();
		form.set("username", "username-group-test");
		form.set("role", "user,newgroup,test");

		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals("username-group-test", object.get("username"));
		assertEquals("user,newgroup,test", object.get("role"));
		resource.release();

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
