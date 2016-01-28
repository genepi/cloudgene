package cloudgene.mapred.api.v2.jobs;

import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.ext.html.FormDataSet;
import org.restlet.resource.ClientResource;

import cloudgene.mapred.api.v2.JobsApiTestCase;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.TestCluster;
import cloudgene.mapred.util.TestServer;

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
		String id = submitJob("long-sleep", form);

		Thread.sleep(8000);

		// Handle answer!
		cancelJob(id);
		// get details
		JSONObject result = getJobDetails(id);
		assertEquals(AbstractJob.STATE_CANCELED, result.get("state"));

	}

	public void testCancelWithoutJobId() throws Exception {

		// Handle answer!
		ClientResource resource = createClientResource("/jobs/cancel");

		Form formStatus = new Form();
		try {
			resource.post(formStatus);
		} catch (Exception e) {

		}
		assertEquals(404, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity()
				.getText());
		assertEquals(object.get("success"), false);
		assertEquals(object.get("message"), "No job id specified.");

	}

	public void testCancelWrongJobId() throws Exception {

		// Handle answer!
		ClientResource resource = createClientResource("/jobs/cancel");

		String id = "some-random-id";

		Form formStatus = new Form();
		formStatus.set("id", id);

		try {
			resource.post(formStatus);
		} catch (Exception e) {

		}
		assertEquals(404, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity()
				.getText());
		assertEquals(object.get("success"), false);
		assertEquals(object.get("message"), "Job " + id + " not found.");

	}

}
