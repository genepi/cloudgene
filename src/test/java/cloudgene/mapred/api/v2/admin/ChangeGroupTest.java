package cloudgene.mapred.api.v2.admin;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.resource.ClientResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.junit.JobsApiTestCase;
import cloudgene.mapred.util.junit.LoginToken;
import cloudgene.mapred.util.junit.TestMailServer;
import cloudgene.mapred.util.junit.TestServer;
import genepi.db.Database;

public class ChangeGroupTest extends JobsApiTestCase {

	@Override
	protected void setUp() throws Exception {
		TestServer.getInstance().start();
		TestMailServer.getInstance().start();

		// insert two dummy users
		Database database = TestServer.getInstance().getDatabase();
		UserDao userDao = new UserDao(database);

		User testUser3 = new User();
		testUser3.setUsername("username-group-test");
		testUser3.setFullName("test1");
		testUser3.setMail("testuser1@test.com");
		testUser3.setRoles(new String[] { "User" });
		testUser3.setActive(true);
		testUser3.setPassword(HashUtil.getMD5("oldpassword"));
		userDao.insert(testUser3);

	}

	public void testWithWrongCredentials() throws JSONException, IOException {

		LoginToken token = login("username-group-test", "oldpassword");

		Database database = TestServer.getInstance().getDatabase();
		UserDao userDao = new UserDao(database);

		User oldUser = userDao.findByUsername("username-group-test");

		// try to update with no credentials
		ClientResource resource = createClientResource("/api/v2/admin/users/changegroup", token);
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

	public void testWithAdminCredentials() throws JSONException, IOException {

		Database database = TestServer.getInstance().getDatabase();
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

		LoginToken token = login("admin", "admin1978");

		// try to update invalid password
		ClientResource resource = createClientResource("/api/v2/admin/users/changegroup", token);
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
