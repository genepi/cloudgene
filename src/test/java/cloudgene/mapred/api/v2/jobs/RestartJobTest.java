package cloudgene.mapred.api.v2.jobs;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.ext.html.FormDataSet;
import org.restlet.resource.ClientResource;

import cloudgene.mapred.api.v2.ApiTestCase;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.TestEnvironment;

public class RestartJobTest extends ApiTestCase {

	@Override
	protected void setUp() throws Exception {
		TestEnvironment.getInstance().startWebServer();

	}

	public void testRestartWriteTextToFileJob() throws Exception {

		// form data

		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		form.add("input-inputtext", "lukas_text");

		// submit job
		String id = submitJob("write-text-to-file", form);

		Thread.sleep(500);

		// stop engine
		TestEnvironment.getInstance().reStartWebServer();

		// get details
		JSONObject result = getJobDetails(id);
		assertEquals(AbstractJob.STATE_DEAD, result.get("state"));

		// restart job
		restartJob(id);

		// check feedback
		waitForJob(id);

		// TODO: change!
		Thread.sleep(5000);

		// get details
		result = getJobDetails(id);

		assertEquals(AbstractJob.STATE_SUCCESS, result.get("state"));

		// get path and download file
		String path = result.getJSONArray("outputParams").getJSONObject(0)
				.getJSONArray("files").getJSONObject(0).getString("path");

		String content = getJobResults(path);

		assertEquals("lukas_text", content);

	}

	public String submitJob(String tool, FormDataSet form)
			throws JSONException, IOException {

		ClientResource resource = createClientResource("/jobs/newsubmit/"
				+ tool);

		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());

		JSONObject object = new JSONObject(resource.getResponseEntity()
				.getText());
		assertEquals(object.get("success"), true);

		return (String) object.get("id");
	}

	public String restartJob(String id) throws JSONException, IOException {

		ClientResource resource = createClientResource("/jobs/restart");

		Form formStatus = new Form();
		formStatus.set("id", id);

		resource.post(formStatus);
		assertEquals(200, resource.getStatus().getCode());

		JSONObject object = new JSONObject(resource.getResponseEntity()
				.getText());
		assertEquals(object.get("success"), true);

		return (String) object.get("id");
	}

	public void waitForJob(String id) throws IOException, JSONException,
			InterruptedException {

		ClientResource resourceStatus = createClientResource("/jobs/newstate");
		Form formStatus = new Form();
		formStatus.set("job_id", id);

		resourceStatus.post(formStatus);

		assertEquals(200, resourceStatus.getStatus().getCode());
		JSONObject object = new JSONObject(resourceStatus.getResponseEntity()
				.getText());
		boolean running = object.getBoolean("running");
		if (running) {
			Thread.sleep(1000);
			waitForJob(id);
		}

	}

	public JSONObject getJobDetails(String id) throws IOException,
			JSONException, InterruptedException {

		ClientResource resourceStatus = createClientResource("/jobs/details");
		Form formStatus = new Form();
		formStatus.set("id", id);

		resourceStatus.post(formStatus);

		assertEquals(200, resourceStatus.getStatus().getCode());
		JSONObject object = new JSONObject(resourceStatus.getResponseEntity()
				.getText());
		return object;

	}

	public String getJobResults(String path) throws IOException, JSONException,
			InterruptedException {

		ClientResource resourceDownload = createClientResource("/results/"
				+ path);

		try {
			resourceDownload.get();
			assertEquals(200, resourceDownload.getStatus().getCode());
			return resourceDownload.getResponseEntity().getText();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(resourceDownload.getResponseEntity().getText());
			assertEquals(false, true);
			return null;
		}

	}

}
