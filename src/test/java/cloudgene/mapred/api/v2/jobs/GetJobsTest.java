package cloudgene.mapred.api.v2.jobs;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import cloudgene.mapred.TestApplication;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.CloudgeneClientRestAssured;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;
import jakarta.inject.Inject;

@MicronautTest
public class GetJobsTest {

	@Inject
	TestApplication application;

	@Inject
	CloudgeneClientRestAssured client;

	@Test
	public void testGetJobsWithoutLogin() {

		RestAssured.when().get("/api/v2/jobs").then().statusCode(401);

	}

	@Test
	public void testGetJobsAsAdminUser() {

		Header accessToken = client.login("admin", "admin1978");

		RestAssured.given().header(accessToken).when().get("/api/v2/jobs").then().statusCode(200);

	}

	@Test
	public void testGetJobsAsAdminUserAndSubmit() {

		Header accessToken = client.login("admin", "admin1978");

		// get list of jobs before submit
		Response response = RestAssured.given().header(accessToken).when().get("/api/v2/jobs").thenReturn();
		List<Object> jobsBefore = response.body().jsonPath().getList("data");

		// submit new job
		response = RestAssured.given().header(accessToken).and().multiPart("input", "input-file")
				.post("/api/v2/jobs/submit/return-true-step-public").thenReturn();
		String id = response.getBody().jsonPath().getString("id");

		// wait until job is complete
		client.waitForJob(id, accessToken);

		// check job state
		RestAssured.given().when().header(accessToken).get("/api/v2/jobs/" + id).then().statusCode(200).and()
				.body("state", equalTo(AbstractJob.STATE_SUCCESS));

		// get list of jobs after submit
		response = RestAssured.given().when().header(accessToken).get("/api/v2/jobs").thenReturn();
		List<Object> jobsAfter = response.body().jsonPath().getList("data");

		assertEquals(jobsBefore.size() + 1, jobsAfter.size());

	}

}
