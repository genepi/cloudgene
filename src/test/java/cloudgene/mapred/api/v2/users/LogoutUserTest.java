package cloudgene.mapred.api.v2.users;

import java.io.IOException;

import org.json.JSONException;
import org.restlet.data.CookieSetting;
import org.restlet.resource.ClientResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.junit.JobsApiTestCase;
import cloudgene.mapred.util.junit.TestServer;
import genepi.db.Database;

public class LogoutUserTest extends JobsApiTestCase {

	@Override
	protected void setUp() throws Exception {
		TestServer.getInstance().start();

		// insert two dummy users
		Database database = TestServer.getInstance().getDatabase();
		UserDao userDao = new UserDao(database);

		User testUser1 = new User();
		testUser1.setUsername("testuser99");
		testUser1.setFullName("test1");
		testUser1.setMail("testuser1@test.com");
		testUser1.setRole("User");
		testUser1.setActive(true);
		testUser1.setActivationCode("");
		testUser1.setPassword(HashUtil.getMD5("testuser99"));
		userDao.insert(testUser1);

	}

	public void testLogout() throws JSONException, IOException {

		CookieSetting cookie = getCookieForUser("testuser99", "testuser99");
		
		//test protected resource
		ClientResource resource = createClientResource("/api/v2/users/testuser99/profile");
		resource.getCookies().add(cookie);
		try {
			resource.get();
		} catch (Exception e) {

		}
		assertEquals(200, resource.getStatus().getCode());
		
		//logout
		resource = createClientResource("/logout");
		try {
			resource.get();
		} catch (Exception e) {

		}
		assertEquals(200, resource.getStatus().getCode());
		
		//test protected resource again
		resource = createClientResource("/api/v2/users/testuser99/profile");
		try {
			resource.get();
		} catch (Exception e) {

		}
		assertEquals(401, resource.getStatus().getCode());
	}

	

}
