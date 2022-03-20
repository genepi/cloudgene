package cloudgene.mapred.api.v2.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.restlet.ext.html.FormData;
import org.restlet.ext.html.FormDataSet;
import org.restlet.resource.ClientResource;

import cloudgene.mapred.TestApplication;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.CloudgeneClient;
import cloudgene.mapred.util.LoginToken;
import cloudgene.mapred.util.TestCluster;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GetJobsTest {

	@Inject
	TestApplication application;
	
	@Inject
	CloudgeneClient client;

	@BeforeAll
	protected void setUp() throws Exception {
		TestCluster.getInstance().start();
	}

	@Test
	public void testGetJobsAsPublicUser() throws IOException, JSONException, InterruptedException {

		ClientResource resourceJobs = client.createClientResource("/api/v2/jobs");

		try {
			resourceJobs.get();
		} catch (Exception e) {
		}
		assertNotSame(200, resourceJobs.getStatus().getCode());
		resourceJobs.release();

	}

	@Test
	public void testGetJobsAsAdminUser() throws IOException, JSONException, InterruptedException {

		LoginToken token = client.login("admin", "admin1978");

		ClientResource resourceJobs = client.createClientResource("/api/v2/jobs", token);

		try {
			resourceJobs.get();
		} catch (Exception e) {
		}

		assertEquals(200, resourceJobs.getStatus().getCode());
		resourceJobs.release();

	}

	@Test
	public void testGetJobsAsAdminUserAndSubmit() throws IOException, JSONException, InterruptedException {

		LoginToken token = client.login("admin", "admin1978");

		JSONArray jobsBefore = client.getJobs(token);

		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		form.getEntries().add(new FormData("input-input", "input-file"));

		// submit job
		String id = client.submitJob("return-true-step-public", form, token);

		// check feedback
		client.waitForJob(id, token);

		JSONObject result = client.getJobDetails(id, token);

		assertEquals(AbstractJob.STATE_SUCCESS, result.get("state"));

		JSONArray jobsAfter = client.getJobs(token);

		assertEquals(jobsBefore.length() + 1, jobsAfter.length());

	}

	// TODO: wrong permissions

	// TODO: wrong id

}
