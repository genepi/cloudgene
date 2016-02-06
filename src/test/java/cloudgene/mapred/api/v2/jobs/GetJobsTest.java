package cloudgene.mapred.api.v2.jobs;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.ext.html.FormData;
import org.restlet.ext.html.FormDataSet;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

import cloudgene.mapred.api.v2.JobsApiTestCase;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.junit.TestCluster;
import cloudgene.mapred.util.junit.TestServer;

public class GetJobsTest extends JobsApiTestCase {

	@Override
	protected void setUp() throws Exception {
		TestServer.getInstance().start();
		TestCluster.getInstance().start();
	}

	public void testGetJobsAsPublicUser() throws IOException, JSONException,
			InterruptedException {

		ClientResource resourceJobs = createClientResource("/jobs");

		try {
			resourceJobs.get();
		} catch (Exception e) {
		}

		assertEquals(401, resourceJobs.getStatus().getCode());

	}

	public void testGetJobsAsAdminUser() throws IOException, JSONException,
			InterruptedException {

		CookieSetting loginCookie = getCookieForUser("admin", "admin1978");

		ClientResource resourceJobs = createClientResource("/jobs");
		resourceJobs.getCookies().add(loginCookie);

		try {
			resourceJobs.get();
		} catch (Exception e) {
		}

		assertEquals(200, resourceJobs.getStatus().getCode());

	}

	public void testGetJobsAsAdminUserAndSubmit() throws IOException,
			JSONException, InterruptedException {

		CookieSetting loginCookie = getCookieForUser("admin", "admin1978");

		JSONArray jobsBefore = getJobs(loginCookie);

		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		form.getEntries().add(new FormData("input-input", "input-file"));

		// submit job
		String id = submitJob("return-true-step-public", form, loginCookie);

		// check feedback
		waitForJob(id, loginCookie);

		JSONObject result = getJobDetails(id, loginCookie);

		assertEquals(AbstractJob.STATE_SUCCESS, result.get("state"));

		JSONArray jobsAfter = getJobs(loginCookie);

		assertEquals(jobsBefore.length() + 1, jobsAfter.length());

	}

	// TODO: wrong permissions

	// TODO: wrong id

}
