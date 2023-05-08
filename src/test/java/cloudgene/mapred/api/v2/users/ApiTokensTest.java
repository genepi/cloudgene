package cloudgene.mapred.api.v2.users;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import cloudgene.mapred.TestApplication;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.CloudgeneClientRestAssured;
import cloudgene.mapred.util.HashUtil;
import genepi.db.Database;
import genepi.io.FileUtil;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;
import jakarta.inject.Inject;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ApiTokensTest {

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
		testUser1.setUsername("testusertoken");
		testUser1.setFullName("test1");
		testUser1.setMail("test1@test.com");
		testUser1.setRoles(new String[] { "private" });
		testUser1.setActive(true);
		testUser1.setActivationCode("");
		testUser1.setPassword(HashUtil.hashPassword("Test1Password"));
		userDao.insert(testUser1);

		User testUser2 = new User();
		testUser2.setUsername("testusertoken2");
		testUser2.setFullName("test2");
		testUser2.setMail("test1@test.com");
		testUser2.setRoles(new String[] { "private" });
		testUser2.setActive(true);
		testUser2.setActivationCode("");
		testUser2.setPassword(HashUtil.hashPassword("Test2Password"));
		userDao.insert(testUser2);

	}

	@Test
	public void testValidateToken() throws InterruptedException {

		Header accessToken = client.login("testusertoken", "Test1Password");

		// check if token is empty
		RestAssured.given().header(accessToken).when().get("/api/v2/users/testusertoken/profile").then().statusCode(200)
				.and().body("username", equalTo("testusertoken")).and().body("hasApiToken", equalTo(false))
				.body("password", nullValue());

		// create token without authentification
		RestAssured.when().post("/api/v2/users/testusertoken/api-token").then().statusCode(401);

		// create token
		String apiToken = RestAssured.given().header(accessToken).when().post("/api/v2/users/testusertoken/api-token")
				.then().statusCode(200).and().body("success", equalTo(true)).and().body("token", not(emptyString()))
				.and().extract().jsonPath().getString("token");

		// validate token
		Map<String, String> form = new HashMap<String, String>();
		form.put("token", apiToken);
		RestAssured.given().formParams(form).when().post("/api/v2/tokens/verify").then().statusCode(200).and()
				.body("valid", equalTo(true));

		// try to revoke token with API token not loginToken. should fail.
		Header headerApiToken = new Header("X-Auth-Token", apiToken);
		RestAssured.given().header(headerApiToken).when().delete("/api/v2/users/testusertoken/api-token").then()
				.statusCode(403);

		// try to update user profile (e.g. email) wit apiToken. should fail.
		Map<String, String> formProfile = new HashMap<String, String>();
		formProfile.put("username", "testusertoken");
		formProfile.put("full-name", "new full-name");
		formProfile.put("mail", "new@email.com");
		formProfile.put("new-password", "new-Password27");
		formProfile.put("confirm-new-password", "new-Password27");

		RestAssured.given().header(headerApiToken).and().formParams(formProfile).when()
				.post("/api/v2/users/testusertoken/profile").then().statusCode(403);

		// revoke token
		RestAssured.given().header(accessToken).when().delete("/api/v2/users/testusertoken/api-token").then()
				.statusCode(200).and().body("success", equalTo(true));

		// validate token
		RestAssured.given().formParams(form).when().post("/api/v2/tokens/verify").then().statusCode(200).and()
				.body("valid", equalTo(false));

	}

	@Test
	public void testCreateTokenWithCorrectCredentials() throws InterruptedException {

		Header accessToken = client.login("testusertoken", "Test1Password");

		// check if token is empty
		RestAssured.given().header(accessToken).when().get("/api/v2/users/testusertoken/profile").then().statusCode(200)
				.and().body("username", equalTo("testusertoken")).and().body("hasApiToken", equalTo(false))
				.body("password", nullValue());

		// create token
		String apiToken = RestAssured.given().header(accessToken).when().post("/api/v2/users/testusertoken/api-token")
				.then().statusCode(200).and().body("success", equalTo(true)).and().body("token", not(emptyString()))
				.and().extract().jsonPath().getString("token");

		// submit job
		Response job = submitTestJob(apiToken);
		String id = job.body().jsonPath().getString("id");

		// check feedback
		client.waitForJobWithApiToken(id, apiToken);

		// check state with apiToken

		Header apiTokenHeader = new Header("X-Auth-Token", apiToken);
		RestAssured.given().header(apiTokenHeader).when().get("/api/v2/jobs/" + id).then().statusCode(200).and()
				.body("state", equalTo(AbstractJob.STATE_SUCCESS));

		// check if job list contains one job
		Response response = RestAssured.given().when().header(apiTokenHeader).get("/api/v2/jobs").thenReturn();
		List<Object> jobs = response.body().jsonPath().getList("data");
		assertEquals(1, jobs.size());

		// revoke token
		RestAssured.given().header(accessToken).when().delete("/api/v2/users/testusertoken/api-token").then()
				.statusCode(200).and().body("success", equalTo(true));

		// validate token
		Map<String, String> form = new HashMap<String, String>();
		form.put("token", apiToken);
		RestAssured.given().formParams(form).when().post("/api/v2/tokens/verify").then().statusCode(200).and()
				.body("valid", equalTo(false));

		// rerun job, should fail
	}

	@Test
	public void testSubmitWithoutVersion() throws InterruptedException {

		Header accessToken = client.login("testusertoken2", "Test2Password");

		// check if token is empty
		RestAssured.given().header(accessToken).when().get("/api/v2/users/testusertoken2/profile").then()
				.statusCode(200).and().body("username", equalTo("testusertoken2")).and()
				.body("hasApiToken", equalTo(false)).body("password", nullValue());

		// create token
		String apiToken = RestAssured.given().header(accessToken).when().post("/api/v2/users/testusertoken/api-token")
				.then().statusCode(200).and().body("success", equalTo(true)).and().body("token", not(emptyString()))
				.and().extract().jsonPath().getString("token");

		// submit job
		Response job = submitTestJobWithoutVersion(apiToken);
		String id = job.body().jsonPath().getString("id");

		// check feedback
		client.waitForJobWithApiToken(id, apiToken);

		// check state with apiToken
		Header apiTokenHeader = new Header("X-Auth-Token", apiToken);
		RestAssured.given().header(apiTokenHeader).when().get("/api/v2/jobs/" + id).then().statusCode(200).and()
				.body("state", equalTo(AbstractJob.STATE_SUCCESS));

		// check if job list contains one job
		Response response = RestAssured.given().when().header(apiTokenHeader).get("/api/v2/jobs").thenReturn();
		List<Object> jobs = response.body().jsonPath().getList("data");
		assertEquals(1, jobs.size());

		// revoke token
		RestAssured.given().header(accessToken).when().delete("/api/v2/users/testusertoken/api-token").then()
				.statusCode(200).and().body("success", equalTo(true));

		// validate token
		Map<String, String> form = new HashMap<String, String>();
		form.put("token", apiToken);
		RestAssured.given().formParams(form).when().post("/api/v2/tokens/verify").then().statusCode(200).and()
				.body("valid", equalTo(false));
	}

	@Test
	public void testSubmitTokenWithWrongApiToken() throws InterruptedException {

		submitTestJob("Wrong Token").then().statusCode(401);

	}

	@Test
	public void testSubmitJobWithExpiredApiToken() throws InterruptedException {

		int expiration = 0;
		Header accessToken = client.login("testusertoken", "Test1Password");

		// create token
		String apiToken = RestAssured.given().header(accessToken).when().post("/api/v2/users/testusertoken/api-token?expiration=" + expiration)
				.then().statusCode(200).and().body("success", equalTo(true)).and().body("token", not(emptyString()))
				.and().extract().jsonPath().getString("token");
		
		// submit job
		submitTestJob(apiToken).then().statusCode(401);

		// revoke token
		RestAssured.given().header(accessToken).when().delete("/api/v2/users/testusertoken/api-token").then()
				.statusCode(200).and().body("success", equalTo(true));

		// validate token
		Map<String, String> form = new HashMap<String, String>();
		form.put("token", apiToken);
		RestAssured.given().formParams(form).post("/api/v2/tokens/verify").then().statusCode(200).and().body("valid",
				equalTo(false));

	}

	private Response submitTestJob(String apiToken) {

		Header accessToken = new Header("X-Auth-Token", apiToken);

		// local-file
		FileUtil.writeStringBufferToFile("test.txt", new StringBuffer("content-of-my-file"));
		FileUtil.writeStringBufferToFile("test1.txt", new StringBuffer("content-of-my-file-in-folder1"));
		FileUtil.writeStringBufferToFile("test2.txt", new StringBuffer("content-of-my-file-in-folder2"));

		// submit job with different inputs and file uploads
		return RestAssured.given().header(accessToken).and().multiPart("job-name", "my-job-name").and()
				.multiPart("input-text", "my-text").and().multiPart("input-number", "27").and()
				.multiPart("input-list", "keya").and().multiPart("input-file", new File("test.txt")).and()
				.multiPart("input-folder", new File("test1.txt")).and().multiPart("input-folder", new File("test2.txt"))
				.when().post("/api/v2/jobs/submit/all-possible-inputs-private").thenReturn();

	}

	private Response submitTestJobWithoutVersion(String apiToken) {

		Header accessToken = new Header("X-Auth-Token", apiToken);

		// local-file
		FileUtil.writeStringBufferToFile("test.txt", new StringBuffer("content-of-my-file"));
		FileUtil.writeStringBufferToFile("test1.txt", new StringBuffer("content-of-my-file-in-folder1"));
		FileUtil.writeStringBufferToFile("test2.txt", new StringBuffer("content-of-my-file-in-folder2"));

		// submit job with different inputs and file uploads
		return RestAssured.given().header(accessToken).and().multiPart("job-name", "my-job-name").and()
				.multiPart("input-text", "my-text").and().multiPart("input-number", "27").and()
				.multiPart("input-list", "keya").and().multiPart("input-file", new File("test.txt")).and()
				.multiPart("input-folder", new File("test1.txt")).and().multiPart("input-folder", new File("test2.txt"))
				.when().post("/api/v2/jobs/submit/app-version-test").thenReturn();

	}

}
