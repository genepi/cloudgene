package cloudgene.mapred.api.v2.jobs;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import cloudgene.mapred.TestApplication;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.CloudgeneClientRestAssured;
import cloudgene.mapred.util.HashUtil;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;
import jakarta.inject.Inject;

@MicronautTest
public class ShareResultsTest {

	@Inject
	TestApplication application;

	@Inject
	CloudgeneClientRestAssured client;

	@Test
	public void testShareSingleFile() throws InterruptedException {

		Header accessToken = client.loginAsPublicUser();

		// submit job
		Response response = RestAssured.given().header(accessToken).and().multiPart("inputtext", "lukas_text").when()
				.post("/api/v2/jobs/submit/write-text-to-file").thenReturn();

		String id = response.getBody().jsonPath().getString("id");

		// wait until submitted job is complete
		client.waitForJob(id, accessToken);

		// TODO: check why file is not available without this sleep
		Thread.sleep(5000);

		// get details
		response = RestAssured.given().header(accessToken).when().get("/api/v2/jobs/" + id).thenReturn();
		response.then().statusCode(200).and().body("state", equalTo(AbstractJob.STATE_SUCCESS)).and()
				.body("outputParams[0].name", equalTo("output")).and().body("outputParams[0].files.size()", equalTo(1));

		String path = response.body().jsonPath().getString("outputParams[0].files[0].path");
		String name = response.jsonPath().getString("outputParams[0].files[0].name");
		String hash = response.jsonPath().getString("outputParams[0].files[0].hash");

		assertEquals(id + "/output/output", path);

		// download file and check content
		RestAssured.given().header(accessToken).when().get("/share/results/" + hash + "/" + name).then().statusCode(200)
				.and().body(equalTo("lukas_text"));

		// check if it returns 404
		String randomHash = HashUtil.getSha256("random-text");
		RestAssured.given().header(accessToken).when().get("/share/results/" + randomHash + "/" + name).then()
				.statusCode(404).body("success", equalTo(false)).and().body("message", equalTo("download not found."));

	}

	@Test
	public void testShareSingleFolder() throws InterruptedException {

		Header accessToken = client.loginAsPublicUser();

		// submit job
		Response response = RestAssured.given().header(accessToken).and().multiPart("inputtext", "lukas_text").when()
				.post("/api/v2/jobs/submit/write-files-to-folder").thenReturn();

		String id = response.getBody().jsonPath().getString("id");

		// wait until submitted job is complete
		client.waitForJob(id, accessToken);

		// TODO: check why file is not available without this sleep
		Thread.sleep(5000);

		// get details
		response = RestAssured.given().header(accessToken).when().get("/api/v2/jobs/" + id).thenReturn();
		response.then().statusCode(200).and().body("state", equalTo(AbstractJob.STATE_SUCCESS)).and()
				.body("outputParams[0].name", equalTo("output")).and().body("outputParams[0].files.size()", equalTo(5));

		// get path and download all 5 files
		for (int i = 0; i < 5; i++) {
			String path = response.body().jsonPath().getString("outputParams[0].files[" + i + "].path");
			String name = response.jsonPath().getString("outputParams[0].files[" + i + "].name");
			String hash = response.jsonPath().getString("outputParams[0].files[" + i + "].hash");

			assertEquals(id + "/output/file" + (i + 1) + ".txt", path);
			RestAssured.given().header(accessToken).when().get("/share/results/" + hash + "/" + name).then()
					.statusCode(200).and().body(equalTo("lukas_text"));
		}

	}

	@Test
	public void testShareCounter() throws InterruptedException {

		Header accessToken = client.loginAsPublicUser();

		// submit job
		Response response = RestAssured.given().header(accessToken).and().multiPart("inputtext", "lukas_text").when()
				.post("/api/v2/jobs/submit/write-files-to-folder").thenReturn();

		String id = response.getBody().jsonPath().getString("id");

		// wait until submitted job is complete
		client.waitForJob(id, accessToken);

		// TODO: check why file is not available without this sleep
		Thread.sleep(5000);

		// get details
		response = RestAssured.given().header(accessToken).when().get("/api/v2/jobs/" + id).thenReturn();
		response.then().statusCode(200).and().body("state", equalTo(AbstractJob.STATE_SUCCESS)).and()
				.body("outputParams[0].name", equalTo("output")).and().body("outputParams[0].files.size()", equalTo(5));

		String path = response.body().jsonPath().getString("outputParams[0].files[0].path");
		String name = response.jsonPath().getString("outputParams[0].files[0].name");
		String hash = response.jsonPath().getString("outputParams[0].files[0].hash");

		assertEquals(id + "/output/file1.txt", path);

		int maxDownloads = application.getSettings().getMaxDownloads();
		// download file max_download
		for (int i = 0; i < maxDownloads; i++) {
			// download file and check content
			RestAssured.given().header(accessToken).when().get("/share/results/" + hash + "/" + name).then()
					.statusCode(200).and().body(equalTo("lukas_text"));
		}

		RestAssured.given().header(accessToken).when().get("/share/results/" + hash + "/" + name).then().statusCode(400)
				.and().body("success", equalTo(false)).and()
				.body("message", equalTo("number of max downloads exceeded."));

	}
}
