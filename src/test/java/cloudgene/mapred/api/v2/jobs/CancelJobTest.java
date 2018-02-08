package cloudgene.mapred.api.v2.jobs;

import org.json.JSONObject;
import org.restlet.ext.html.FormDataSet;
import org.restlet.resource.ClientResource;

import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.junit.JobsApiTestCase;
import cloudgene.mapred.util.junit.TestCluster;
import cloudgene.mapred.util.junit.TestServer;

public class CancelJobTest extends JobsApiTestCase {

	@Override
	protected void setUp() throws Exception {
		TestServer.getInstance().start();
		TestCluster.getInstance().start();
	}

	public void testCancelSleepJob() throws Exception {

		// form data

		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		form.add("input", "dummy");

		// submit job
		String id = submitJobPublic("long-sleep", form);

		Thread.sleep(8000);

		// Handle answer!
		cancelJob(id);
		// get details
		JSONObject result = getJobDetails(id);
		assertEquals(AbstractJob.STATE_CANCELED, result.get("state"));

	}

	public void testCancelWrongJobId() throws Exception {

		String id = "some-random-id";

		// Handle answer!
		ClientResource resource = createClientResource("/api/v2/jobs/" + id
				+ "/cancel");

		try {
			resource.get();
		} catch (Exception e) {

		}
		assertEquals(404, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity()
				.getText());
		assertEquals(object.get("success"), false);
		assertEquals(object.get("message"), "Job " + id + " not found.");
		resource.release();
	}

}
