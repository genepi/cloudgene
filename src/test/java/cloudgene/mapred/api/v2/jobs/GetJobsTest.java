package cloudgene.mapred.api.v2.jobs;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.html.FormData;
import org.restlet.ext.html.FormDataSet;
import org.restlet.resource.ClientResource;

import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.junit.JobsApiTestCase;
import cloudgene.mapred.util.junit.LoginToken;
import cloudgene.mapred.util.junit.TestCluster;
import cloudgene.mapred.util.junit.TestServer;

public class GetJobsTest extends JobsApiTestCase {

	@Override
	protected void setUp() throws Exception {
		TestServer.getInstance().start();
		TestCluster.getInstance().start();
	}

	public void testGetJobsAsPublicUser() throws IOException, JSONException, InterruptedException {

		ClientResource resourceJobs = createClientResource("/api/v2/jobs");

		try {
			resourceJobs.get();
		} catch (Exception e) {
		}
		assertNotSame(200, resourceJobs.getStatus().getCode());
		resourceJobs.release();

	}

	public void testGetJobsAsAdminUser() throws IOException, JSONException, InterruptedException {

		LoginToken token = login("admin", "admin1978");

		ClientResource resourceJobs = createClientResource("/api/v2/jobs", token);

		try {
			resourceJobs.get();
		} catch (Exception e) {
		}

		assertEquals(200, resourceJobs.getStatus().getCode());
		resourceJobs.release();

	}

	public void testGetJobsAsAdminUserAndSubmit() throws IOException, JSONException, InterruptedException {

		LoginToken token = login("admin", "admin1978");

		JSONArray jobsBefore = getJobs(token);

		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		form.getEntries().add(new FormData("input-input", "input-file"));

		// submit job
		String id = submitJob("return-true-step-public", form, token);

		// check feedback
		waitForJob(id, token);

		JSONObject result = getJobDetails(id, token);

		assertEquals(AbstractJob.STATE_SUCCESS, result.get("state"));

		JSONArray jobsAfter = getJobs(token);

		assertEquals(jobsBefore.length() + 1, jobsAfter.length());

	}

	// TODO: wrong permissions

	// TODO: wrong id

}
