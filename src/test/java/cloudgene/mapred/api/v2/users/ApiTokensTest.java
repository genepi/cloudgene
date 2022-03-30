package cloudgene.mapred.api.v2.users;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.ext.html.FormData;
import org.restlet.ext.html.FormDataSet;
import org.restlet.representation.FileRepresentation;
import org.restlet.resource.ClientResource;

import com.google.gson.JsonObject;

import cloudgene.mapred.TestApplication;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.CloudgeneClient;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.LoginToken;
import genepi.db.Database;
import genepi.io.FileUtil;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ApiTokensTest {

	@Inject
	TestApplication application;

	@Inject
	CloudgeneClient client;

	@BeforeAll
	protected void setUp() throws Exception {

		// insert two dummy users
		Database database = application.getDatabase();
		UserDao userDao = new UserDao(database);

		User testUser1 = new User();
		testUser1.setUsername("testUserToken");
		testUser1.setFullName("test1");
		testUser1.setMail("test1@test.com");
		testUser1.setRoles(new String[] { "private" });
		testUser1.setActive(true);
		testUser1.setActivationCode("");
		testUser1.setPassword(HashUtil.hashPassword("Test1Password"));
		userDao.insert(testUser1);

		User testUser2 = new User();
		testUser2.setUsername("testUserToken2");
		testUser2.setFullName("test2");
		testUser2.setMail("test1@test.com");
		testUser2.setRoles(new String[] { "private" });
		testUser2.setActive(true);
		testUser2.setActivationCode("");
		testUser2.setPassword(HashUtil.hashPassword("Test2Password"));
		userDao.insert(testUser2);

	}

	@Test
	public void testValidateToken() throws JSONException, IOException, InterruptedException {

		LoginToken token = client.login("testUserToken", "Test1Password");

		// check if token is empty
		ClientResource resource = client.createClientResource("/api/v2/users/" + "testUserToken" + "/api-token", token);
		try {
			resource.get();
		} catch (Exception e) {

		}
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), true);
		assertEquals(object.get("token"), "");
		resource.release();

		// create token
		resource = client.createClientResource("/api/v2/users/" + "testUserToken" + "/api-token", token);
		try {
			resource.post(new Form());
		} catch (Exception e) {

		}
		assertEquals(200, resource.getStatus().getCode());
		object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), true);
		assertFalse(object.get("token").equals(""));
		resource.release();

		String apiToken = object.getString("token");

		// validate token
		object = client.validateToken(apiToken, token);
		assertTrue(object.getBoolean("valid"));

		// revoke token
		resource = client.createClientResource("/api/v2/users/" + "testUserToken" + "/api-token", token);
		try {
			resource.delete();
		} catch (Exception e) {

		}
		assertEquals(200, resource.getStatus().getCode());
		object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), true);
		resource.release();

		// validate token
		object = client.validateToken(apiToken, token);
		assertFalse(object.getBoolean("valid"));

		
	}
	
	@Test
	public void testCreateTokenWithCorrectCredentials() throws JSONException, IOException, InterruptedException {

		LoginToken token = client.login("testUserToken", "Test1Password");

		// check if token is empty
		ClientResource resource = client.createClientResource("/api/v2/users/" + "testUserToken" + "/api-token", token);
		try {
			resource.get();
		} catch (Exception e) {

		}
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), true);
		assertEquals(object.get("token"), "");
		resource.release();

		// create token
		resource = client.createClientResource("/api/v2/users/" + "testUserToken" + "/api-token", token);
		try {
			resource.post(new Form());
		} catch (Exception e) {

		}
		assertEquals(200, resource.getStatus().getCode());
		object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), true);
		assertFalse(object.get("token").equals(""));

		String apiToken = object.getString("token");

		// submit job
		String id = submitTestJob(apiToken);

		// check feedback
		client.waitForJobWithApiToken(id, apiToken);

		JSONObject result =client. getJobDetailsWithApiToken(id, apiToken);

		assertEquals(AbstractJob.STATE_SUCCESS, result.get("state"));

		resource.release();

		// check if job list contains one job
		JSONArray jobs = client.getJobsWithApiToken(apiToken);
		assertEquals(1, jobs.length());

		// revoke token
		resource = client.createClientResource("/api/v2/users/" + "testUserToken" + "/api-token", token);
		try {
			resource.delete();
		} catch (Exception e) {

		}
		assertEquals(200, resource.getStatus().getCode());
		object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), true);
		resource.release();

		// check if token is invalid now
		try {
			id = submitTestJob(apiToken);
			assertTrue(false);
		} catch (Exception e) {

		}
	}

	@Test
	public void testSubmitWithoutVersion() throws JSONException, IOException, InterruptedException {

		LoginToken token = client.login("testUserToken2", "Test2Password");

		// check if token is empty
		ClientResource resource = client.createClientResource("/api/v2/users/" + "testUserToken2" + "/api-token", token);
		try {
			resource.get();
		} catch (Exception e) {

		}
		assertEquals(200, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), true);
		assertEquals(object.get("token"), "");
		resource.release();

		// create token
		resource = client.createClientResource("/api/v2/users/" + "testUserToken2" + "/api-token", token);
		try {
			resource.post(new Form());
		} catch (Exception e) {

		}
		assertEquals(200, resource.getStatus().getCode());
		object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), true);
		assertFalse(object.get("token").equals(""));

		String apiToken = object.getString("token");

		// submit job
		String id = submitTestJobWithoutVersion(apiToken);

		// check feedback
		client.waitForJobWithApiToken(id, apiToken);

		JSONObject result = client.getJobDetailsWithApiToken(id, apiToken);

		assertEquals(AbstractJob.STATE_SUCCESS, result.get("state"));

		resource.release();

		// check if job list contains one job
		JSONArray jobs = client.getJobsWithApiToken(apiToken);
		assertEquals(1, jobs.length());

		// revoke token
		resource = client.createClientResource("/api/v2/users/" + "testUserToken2" + "/api-token", token);
		try {
			resource.delete();
		} catch (Exception e) {

		}
		assertEquals(200, resource.getStatus().getCode());
		object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), true);
		resource.release();

		// check if token is invalid now
		try {
			id = submitTestJob(apiToken);
			assertTrue(false);
		} catch (Exception e) {

		}
	}

	@Test
	public void testSubmitTokenWithWtongApiToken() throws JSONException, IOException, InterruptedException {

		// submit job
		String id = null;
		try {
			id = submitTestJob("Wrong Token");
			assertTrue(false);
		} catch (Exception e) {
			assertEquals(null, id);
		}

	}

	private String submitTestJob(String apiToken) throws JSONException, IOException {
		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		form.getEntries().add(new FormData("input-text", "my-text"));
		form.getEntries().add(new FormData("input-number", "27"));
		// ignore checkbox
		form.getEntries().add(new FormData("input-list", "keya"));
		// local-file
		FileUtil.writeStringBufferToFile("test.txt", new StringBuffer("content-of-my-file"));
		form.getEntries().add(new FormData("input-file", new FileRepresentation("test.txt", MediaType.TEXT_PLAIN)));

		// local-folder
		FileUtil.writeStringBufferToFile("test1.txt", new StringBuffer("content-of-my-file-in-folder1"));
		FileUtil.writeStringBufferToFile("test2.txt", new StringBuffer("content-of-my-file-in-folder2"));

		form.getEntries().add(new FormData("input-folder", new FileRepresentation("test1.txt", MediaType.TEXT_PLAIN)));
		form.getEntries().add(new FormData("input-folder", new FileRepresentation("test2.txt", MediaType.TEXT_PLAIN)));

		// submit job
		return client.submitJobWithApiToken("all-possible-inputs-private", form, apiToken);

	}

	private String submitTestJobWithoutVersion(String apiToken) throws JSONException, IOException {
		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		form.getEntries().add(new FormData("input-text", "my-text"));
		form.getEntries().add(new FormData("input-number", "27"));
		// ignore checkbox
		form.getEntries().add(new FormData("input-list", "keya"));
		// local-file
		FileUtil.writeStringBufferToFile("test.txt", new StringBuffer("content-of-my-file"));
		form.getEntries().add(new FormData("input-file", new FileRepresentation("test.txt", MediaType.TEXT_PLAIN)));

		// local-folder
		FileUtil.writeStringBufferToFile("test1.txt", new StringBuffer("content-of-my-file-in-folder1"));
		FileUtil.writeStringBufferToFile("test2.txt", new StringBuffer("content-of-my-file-in-folder2"));

		form.getEntries().add(new FormData("input-folder", new FileRepresentation("test1.txt", MediaType.TEXT_PLAIN)));
		form.getEntries().add(new FormData("input-folder", new FileRepresentation("test2.txt", MediaType.TEXT_PLAIN)));

		// submit job
		return client.submitJobWithApiToken("app-version-test", form, apiToken);

	}

}
