package cloudgene.mapred.api.v2.jobs;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;

import java.io.File;

import org.junit.jupiter.api.Test;

import cloudgene.mapred.TestApplication;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.CloudgeneClientRestAssured;
import cloudgene.sdk.internal.WorkflowContext;
import genepi.io.FileUtil;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;
import jakarta.inject.Inject;

@MicronautTest
public class SubmitJobTest {

	@Inject
	TestApplication application;

	@Inject
	CloudgeneClientRestAssured client;

	@Test
	public void testSubmitWithoutLogin() {

		RestAssured.given().multiPart("input", "input-file").when().post("/api/v2/jobs/submit/all-possible-inputs")
				.then().statusCode(401);

	}

	@Test
	public void testSubmitBlockedInMaintenance() {

		Header accessToken = client.loginAsPublicUser();

		// enter maintenance mode
		application.getSettings().setMaintenance(true);

		RestAssured.given().header(accessToken).and().multiPart("input", "input-file").when()
				.post("/api/v2/jobs/submit/all-possible-inputs").then().statusCode(503).and()
				.body("success", equalTo(false)).and()
				.body("message", equalTo("This functionality is currently under maintenance."));

		// exit maintenance mode
		application.getSettings().setMaintenance(false);

	}

	@Test
	public void testSubmitWrongApplication() {

		Header accessToken = client.loginAsPublicUser();

		RestAssured.given().header(accessToken).and().multiPart("input", "input-file").when()
				.post("/api/v2/jobs/submit/wrong-application").then().statusCode(404);

	}

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

		// get details and check state
		RestAssured.given().header(accessToken).when().get("/api/v2/jobs/" + id).then().statusCode(200).and()
				.body("state", equalTo(AbstractJob.STATE_SUCCESS)).and().body("name", equalTo("my-job-name"));

	}

	@Test
	public void testSubmitAllPossibleInputsHdfs() {

		Header accessToken = client.loginAsPublicUser();

		// local-file
		FileUtil.writeStringBufferToFile("test.txt", new StringBuffer("content-of-my-file"));
		FileUtil.writeStringBufferToFile("test1.txt", new StringBuffer("content-of-my-file-in-folder1"));
		FileUtil.writeStringBufferToFile("test2.txt", new StringBuffer("content-of-my-file-in-folder2"));

		// submit job with different inputs and file uploads
		String id = RestAssured.given().header(accessToken).and().multiPart("job-name", "my-job-name").and()
				.multiPart("input-file", new File("test.txt")).and().multiPart("input-folder", new File("test1.txt"))
				.and().multiPart("input-folder", new File("test2.txt")).when()
				.post("/api/v2/jobs/submit/all-possible-inputs-hdfs").then().statusCode(200).and().extract().jsonPath()
				.getString("id");

		// wait until job is complete
		client.waitForJob(id, accessToken);

		// get details and check state
		RestAssured.given().header(accessToken).when().get("/api/v2/jobs/" + id).then().statusCode(200).and()
				.body("state", equalTo(AbstractJob.STATE_SUCCESS)).and().body("name", equalTo("my-job-name"));

	}

	@Test
	public void testSubmitReturnTrueStepPublic() {

		Header accessToken = client.loginAsPublicUser();

		// submit job
		String id = RestAssured.given().header(accessToken).and().multiPart("input", "input-file").when()
				.post("/api/v2/jobs/submit/return-true-step-public").then().statusCode(200).and().extract().jsonPath()
				.getString("id");

		// wait until job is complete
		client.waitForJob(id, accessToken);

		// get details and check state
		RestAssured.given().header(accessToken).when().get("/api/v2/jobs/" + id).then().statusCode(200).and()
				.body("state", equalTo(AbstractJob.STATE_SUCCESS));

	}

	@Test
	public void testSubmitReturnFalseStepPublic() {

		Header accessToken = client.loginAsPublicUser();

		// submit jobs
		String id = RestAssured.given().header(accessToken).and().multiPart("input", "input-file").when()
				.post("/api/v2/jobs/submit/return-false-step-public").then().statusCode(200).and().extract().jsonPath()
				.getString("id");

		// wait until job is complete
		client.waitForJob(id, accessToken);

		// get details and check state
		RestAssured.given().header(accessToken).when().get("/api/v2/jobs/" + id).then().statusCode(200).and()
				.body("state", equalTo(AbstractJob.STATE_FAILED));

	}

	@Test
	public void testSubmitReturnExceptionStepPublic() {

		Header accessToken = client.loginAsPublicUser();

		// submit jobs
		String id = RestAssured.given().header(accessToken).and().multiPart("input", "input-file").when()
				.post("/api/v2/jobs/submit/return-exception-step-public").then().statusCode(200).and().extract()
				.jsonPath().getString("id");

		// wait until job is complete
		client.waitForJob(id, accessToken);

		// get details and check state
		RestAssured.given().header(accessToken).when().get("/api/v2/jobs/" + id).then().statusCode(200).and()
				.body("state", equalTo(AbstractJob.STATE_FAILED));

	}

	@Test
	public void testSubmitWriteTextToFilePublic() throws InterruptedException {

		Header accessToken = client.loginAsPublicUser();

		// submit jobs
		String id = RestAssured.given().header(accessToken).and().multiPart("input-inputtext", "lukas_text").when()
				.post("/api/v2/jobs/submit/write-text-to-file").then().statusCode(200).and().extract().jsonPath()
				.getString("id");

		// wait until job is complete
		client.waitForJob(id, accessToken);

		// TODO: change!
		Thread.sleep(5000);

		Response response = RestAssured.given().header(accessToken).when().get("/api/v2/jobs/" + id).thenReturn();
		response.then().statusCode(200).and().body("state", equalTo(AbstractJob.STATE_SUCCESS));

		// get file details
		String name = response.jsonPath().getString("outputParams[0].files[0].name");
		String hash = response.jsonPath().getString("outputParams[0].files[0].hash");

		// download file and check content
		RestAssured.given().header(accessToken).when().get("/downloads/" + id + "/" + hash + "/" + name).then()
				.statusCode(200).and().body(equalTo("lukas_text"));

	}

	@Test
	public void testSubmitWriteTextToHdfsFilePublic() throws InterruptedException {

		Header accessToken = client.loginAsPublicUser();

		// submit jobs
		String id = RestAssured.given().header(accessToken).and().multiPart("input-inputtext", "lukas_text").when()
				.post("/api/v2/jobs/submit/write-text-to-hdfs-file").then().statusCode(200).and().extract().jsonPath()
				.getString("id");

		// wait until job is complete
		client.waitForJob(id, accessToken);

		// TODO: change!
		Thread.sleep(5000);

		Response response = RestAssured.given().header(accessToken).when().get("/api/v2/jobs/" + id).thenReturn();
		response.then().statusCode(200).and().body("state", equalTo(AbstractJob.STATE_SUCCESS));

		// get file details
		String name = response.jsonPath().getString("outputParams[0].files[0].name");
		String hash = response.jsonPath().getString("outputParams[0].files[0].hash");

		// download file and check content
		RestAssured.given().header(accessToken).when().get("/downloads/" + id + "/" + hash + "/" + name).then()
				.statusCode(200).and().body(equalTo("lukas_text"));

	}

	@Test
	public void testSubmitThreeTasksStepPublic() {

		Header accessToken = client.loginAsPublicUser();

		// submit jobs
		String id = RestAssured.given().header(accessToken).and().multiPart("input-input", "input-file").when()
				.post("/api/v2/jobs/submit/three-tasks").then().statusCode(200).and().extract().jsonPath()
				.getString("id");

		// wait until job is complete
		client.waitForJob(id, accessToken);

		// check if three tasks are in json object
		RestAssured.given().header(accessToken).when().get("/api/v2/jobs/" + id).then().statusCode(200).and()
				.body("state", equalTo(AbstractJob.STATE_SUCCESS)).and()
				.body("steps[0].logMessages[0].message", equalTo("cloudgene-task1")).and()
				.body("steps[0].logMessages[0].type", equalTo(WorkflowContext.OK)).and()
				.body("steps[0].logMessages[1].message", equalTo("cloudgene-task2")).and()
				.body("steps[0].logMessages[1].type", equalTo(WorkflowContext.OK)).and()
				.body("steps[0].logMessages[2].message", equalTo("cloudgene-task3")).and()
				.body("steps[0].logMessages[2].type", equalTo(WorkflowContext.OK));

	}

	@Test
	public void testSubmitWithHiddenInputs() {

		Header accessToken = client.loginAsPublicUser();

		// submit jobs
		String id = RestAssured.given().header(accessToken).and().multiPart("input-checkbox1", "true").when()
				.post("/api/v2/jobs/submit/print-hidden-inputs").then().statusCode(200).and().extract().jsonPath()
				.getString("id");

		// wait until job is complete
		client.waitForJob(id, accessToken);

		// check if all inputs are printed to log
		RestAssured.given().header(accessToken).when().get("/api/v2/jobs/" + id).then().statusCode(200).and()
				.body("state", equalTo(AbstractJob.STATE_SUCCESS)).and().body("steps.size()", equalTo(6)).and()
				.body("steps[0].logMessages[0].message", equalTo("text1: my-value\n")).and()
				.body("steps[1].logMessages[0].message", equalTo("checkbox1: true\n")).and()
				.body("steps[2].logMessages[0].message", equalTo("list1: value1\n")).and()
				.body("steps[3].logMessages[0].message", equalTo("text2: my-value\n")).and()
				.body("steps[4].logMessages[0].message", equalTo("checkbox2: true\n")).and()
				.body("steps[5].logMessages[0].message", equalTo("list2: value1\n"));

	}

	@Test
	public void testSubmitHtmlInParams() {

		String html = "<script>console.log('Hey')<script>";

		Header accessToken = client.loginAsPublicUser();

		// submit jobs
		String id = RestAssured.given().header(accessToken).and().multiPart("text1", "value " + html).when()
				.post("/api/v2/jobs/submit/print-hidden-inputs").then().statusCode(200).and().extract().jsonPath()
				.getString("id");

		// wait until job is complete
		client.waitForJob(id, accessToken);

		// check if html value was escaped
		RestAssured.given().header(accessToken).when().get("/api/v2/jobs/" + id).then().statusCode(200).and()
				.body("state", equalTo(AbstractJob.STATE_SUCCESS)).and()
				.body("steps[0].logMessages[0].message", not(containsString(html)));

	}

}
