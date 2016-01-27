package cloudgene.mapred.api.v2;

import java.io.IOException;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.ext.html.FormDataSet;
import org.restlet.resource.ClientResource;

import cloudgene.mapred.util.TestEnvironment;

public class JobsApiTestCase extends TestCase {

	public ClientResource createClientResource(String path) {
		return new ClientResource(TestEnvironment.HOSTNAME + path);
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

	public String cancelJob(String id) throws JSONException, IOException {

		ClientResource resource = createClientResource("/jobs/cancel");

		Form formStatus = new Form();
		formStatus.set("id", id);

		resource.post(formStatus);
		assertEquals(200, resource.getStatus().getCode());

		// JSONObject object = new JSONObject(resource.getResponseEntity()
		// .getText());
		// assertEquals(object.get("success"), true);

		return "";
	}

	public String deleteJob(String id) throws JSONException, IOException {

		ClientResource resource = createClientResource("/jobs/delete");

		Form formStatus = new Form();
		formStatus.set("id", id);

		resource.post(formStatus);
		assertEquals(200, resource.getStatus().getCode());

		// JSONObject object = new JSONObject(resource.getResponseEntity()
		// .getText());
		// assertEquals(object.get("success"), true);

		return "";
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

	public String downloadResults(String path) throws IOException,
			JSONException, InterruptedException {

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

	public String downloadSharedResults(String user, String hash,
			String filename) throws IOException, JSONException,
			InterruptedException {

		ClientResource resourceDownload = createClientResource("/share/" + user
				+ "/" + hash + "/" + filename);

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
