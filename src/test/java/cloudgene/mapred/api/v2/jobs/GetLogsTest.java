package cloudgene.mapred.api.v2.jobs;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import cloudgene.mapred.TestApplication;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.CloudgeneClientRestAssured;
import genepi.io.FileUtil;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;
import jakarta.inject.Inject;

@MicronautTest
public class GetLogsTest {

	@Inject
	TestApplication application;

	@Inject
	CloudgeneClientRestAssured client;

	@Test
	public void testSubmitAllPossibleInputs() {

		Header accessToken = client.loginAsPublicUser();

		// local-file
		FileUtil.writeStringBufferToFile("test.txt", new StringBuffer("content-of-my-file"));
		FileUtil.writeStringBufferToFile("test1.txt", new StringBuffer("content-of-my-file-in-folder1"));
		FileUtil.writeStringBufferToFile("test2.txt", new StringBuffer("content-of-my-file-in-folder2"));

		// submit job with different inputs and file uploads
		String id = RestAssured.given().header(accessToken).and().multiPart("job-name", "my-job-name").and()
				.multiPart("input-text", "my-text").and().multiPart("input-number", "27").and()
				.multiPart("input-list", "keya").and().multiPart("input-file", new File("test.txt")).and()
				.multiPart("input-folder", new File("test1.txt")).and().multiPart("input-folder", new File("test2.txt"))
				.when().post("/api/v2/jobs/submit/all-possible-inputs").then().statusCode(200).and().extract()
				.jsonPath().getString("id");
		// wait until job is complete
		client.waitForJob(id, accessToken);

		// get details and check state. logs should be empty (used for links etc..)
		Response response = RestAssured.given().header(accessToken).when().get("/api/v2/jobs/" + id).thenReturn();
		response.then().statusCode(200).and().body("state", equalTo(AbstractJob.STATE_SUCCESS)).and()
				.body("name", equalTo("my-job-name")).and().body("state", equalTo(AbstractJob.STATE_SUCCESS)).and()
				.body("logs", emptyString());

		// direct link should work
		response = RestAssured.given().header(accessToken).when().get("/logs/" + id).thenReturn();
		response.then().statusCode(200);
		String content = response.body().asString();

		// check content for some success messages
		assertTrue(content.contains("Cleanup successful."));
		assertTrue(content.contains("Data Export successful."));
		assertTrue(content.contains("Job Execution successful."));
		assertTrue(content.contains("Input-Text: my-text"));
		assertTrue(content.contains("Input-number: 27"));
		assertTrue(content.contains("Input Checkbox: valueFalse"));
		assertTrue(content.contains("Input List: keya"));

		assertTrue(content.contains("Planner: WDL evaluated."));
		assertTrue(content.contains("CheckInputs"));

		// should return 401 without login
		RestAssured.given().when().get("/logs/" + id).then().statusCode(401);

	}

	@Test
	public void testWriteToStdOuStepPublic() {

		Header accessToken = client.loginAsPublicUser();

		// submit job with different inputs and file uploads
		String id = RestAssured.given().header(accessToken).and().multiPart("input-input", "input-file").when()
				.post("/api/v2/jobs/submit/write-text-to-std-out").then().statusCode(200).and().extract()
				.jsonPath().getString("id");

		// wait until job is complete
		client.waitForJob(id, accessToken);

		// get details and check state. logs should be empty (used for links etc..)
		RestAssured.given().header(accessToken).when().get("/api/v2/jobs/" + id).then().statusCode(200).and()
				.body("state", equalTo(AbstractJob.STATE_SUCCESS)).and().body("logs", emptyString());

		// direct link should work
		Response response = RestAssured.given().header(accessToken).when().get("/logs/" + id).thenReturn();
		response.then().statusCode(200);
		String content = response.body().asString();

		assertTrue(content.contains("taks write to system out"));
		assertTrue(content.contains("taks write to system out2"));
		assertTrue(content.contains("taks write to system out3"));

		assertTrue(content.contains("taks write to log"));
		assertTrue(content.contains("taks write to log2"));
		assertTrue(content.contains("taks write to log3"));

		// should return 401 without login
		RestAssured.given().when().get("/logs/" + id).then().statusCode(401);

	}

}
