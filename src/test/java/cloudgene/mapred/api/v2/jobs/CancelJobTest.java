package cloudgene.mapred.api.v2.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.restlet.ext.html.FormDataSet;
import org.restlet.resource.ClientResource;

import cloudgene.mapred.TestApplication;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.CloudgeneClient;
import cloudgene.mapred.util.LoginToken;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;

@MicronautTest
public class CancelJobTest {

	@Inject
	TestApplication application;

	@Inject
	CloudgeneClient client;

	@Test
	public void testCancelSleepJob() throws Exception {

		// form data

		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		form.add("input", "dummy");

		// submit job
		String id = client.submitJobPublic("long-sleep", form);

		Thread.sleep(8000);

		// Handle answer!
		client.cancelJob(id);
		// get details
		JSONObject result = client.getJobDetails(id);
		assertEquals(AbstractJob.STATE_CANCELED, result.get("state"));

	}

	@Test
	public void testCancelWrongJobId() throws Exception {

		String id = "some-random-id";

		LoginToken token = client.loginAsPublicUser();
		ClientResource resource = client.createClientResource("/api/v2/jobs/" + id + "/cancel", token);

		try {
			resource.get();
		} catch (Exception e) {

		}
		assertEquals(404, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertEquals(object.get("message"), "Job " + id + " not found.");
		resource.release();
	}

	@Test
	public void testCancelWithoutLogin() throws Exception {

		String id = "some-random-id";

		ClientResource resource = client.createClientResource("/api/v2/jobs/" + id + "/cancel");

		try {
			resource.get();
		} catch (Exception e) {

		}
		assertEquals(401, resource.getStatus().getCode());
		resource.release();
	}

}
