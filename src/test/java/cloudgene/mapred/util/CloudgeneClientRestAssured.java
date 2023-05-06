package cloudgene.mapred.util;

import java.io.IOException;

import org.json.JSONException;

import cloudgene.mapred.jobs.AbstractJob;
import io.micronaut.context.annotation.Prototype;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@Prototype
public class CloudgeneClientRestAssured {

	public static int POLL_INTERVAL_MS = 500;

	public Header login(String username, String password) {

		Response response = RestAssured.given().formParams("username", "public", "password", "public").when()
				.post("/login").thenReturn();
		response.then().statusCode(200);
		return new Header("X-Auth-Token", response.body().jsonPath().getString("access_token"));
	}

	public Header loginAsPublicUser() {
		return login("public", "public");
	}
	
	public void waitForJob(String id, Header accessToken) throws IOException, JSONException, InterruptedException {

		Response response = RestAssured.given().when().header(accessToken).get("/api/v2/jobs/" + id + "/status")
				.thenReturn();
		response.then().statusCode(200);

		int state = response.body().jsonPath().getInt("state");

		boolean running = state == AbstractJob.STATE_WAITING || state == AbstractJob.STATE_RUNNING
				|| state == AbstractJob.STATE_EXPORTING;
		if (running) {
			Thread.sleep(POLL_INTERVAL_MS);
			waitForJob(id, accessToken);
		}
	}
}
