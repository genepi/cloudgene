package cloudgene.mapred.api.v2;

import java.io.IOException;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.ext.html.FormDataSet;
import org.restlet.resource.ClientResource;

import cloudgene.mapred.util.junit.TestServer;

public class JobsApiTestCase extends TestCase {

	public ClientResource createClientResource(String path) {
		return new ClientResource(TestServer.HOSTNAME + path);
	}

	public ClientResource createClientResource(String path,
			CookieSetting loginCookie) {
		ClientResource resource = createClientResource(path);
		if (loginCookie != null) {
			resource.getCookies().add(loginCookie);
		}
		return resource;
	}

	public String submitJob(String tool, FormDataSet form)
			throws JSONException, IOException {
		return submitJob(tool, form, null);
	}

	public String submitJob(String tool, FormDataSet form,
			CookieSetting loginCookie) throws JSONException, IOException {

		ClientResource resource = createClientResource("/jobs/newsubmit/"
				+ tool, loginCookie);

		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());

		JSONObject object = new JSONObject(resource.getResponseEntity()
				.getText());
		assertEquals(object.get("success"), true);

		return (String) object.get("id");
	}

	public String restartJob(String id) throws JSONException, IOException {
		return restartJob(id, null);
	}

	public String restartJob(String id, CookieSetting loginCookie)
			throws JSONException, IOException {

		ClientResource resource = createClientResource("/jobs/restart",
				loginCookie);

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
		return cancelJob(id, null);
	}

	public String cancelJob(String id, CookieSetting loginCookie)
			throws JSONException, IOException {

		ClientResource resource = createClientResource("/jobs/cancel",
				loginCookie);

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
		return deleteJob(id, null);
	}

	public String deleteJob(String id, CookieSetting loginCookie)
			throws JSONException, IOException {

		ClientResource resource = createClientResource("/jobs/delete",
				loginCookie);

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
		waitForJob(id, null);
	}

	public void waitForJob(String id, CookieSetting loginCookie)
			throws IOException, JSONException, InterruptedException {

		ClientResource resourceStatus = createClientResource("/jobs/newstate",
				loginCookie);
		Form formStatus = new Form();
		formStatus.set("job_id", id);

		resourceStatus.post(formStatus);

		assertEquals(200, resourceStatus.getStatus().getCode());
		JSONObject object = new JSONObject(resourceStatus.getResponseEntity()
				.getText());
		boolean running = object.getBoolean("running");
		if (running) {
			Thread.sleep(1000);
			waitForJob(id, loginCookie);
		}

	}

	public JSONObject getJobDetails(String id) throws IOException,
			JSONException, InterruptedException {
		return getJobDetails(id, null);
	}

	public JSONObject getJobDetails(String id, CookieSetting loginCookie)
			throws IOException, JSONException, InterruptedException {

		ClientResource resourceStatus = createClientResource("/jobs/details",
				loginCookie);
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
		return downloadResults(path, null);
	}

	public String downloadResults(String path, CookieSetting loginCookie)
			throws IOException, JSONException, InterruptedException {

		ClientResource resourceDownload = createClientResource("/results/"
				+ path, loginCookie);

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
		return downloadSharedResults(user, hash, filename, null);
	}

	public String downloadSharedResults(String user, String hash,
			String filename, CookieSetting loginCookie) throws IOException,
			JSONException, InterruptedException {

		ClientResource resourceDownload = createClientResource("/share/" + user
				+ "/" + hash + "/" + filename, loginCookie);

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

	public String downloadURL(String url) throws IOException, JSONException,
			InterruptedException {
		return downloadURL(url, null);
	}

	public String downloadURL(String url, CookieSetting loginCookie)
			throws IOException, JSONException, InterruptedException {

		ClientResource resourceDownload = createClientResource(url, loginCookie);

		try {
			resourceDownload.get();
			assertEquals(200, resourceDownload.getStatus().getCode());
			return resourceDownload.getResponseEntity().getText();
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(false, true);
			return null;
		}

	}

	public JSONArray getJobs(CookieSetting loginCookie) throws JSONException, IOException{{

		ClientResource resourceJobs = createClientResource("/jobs");
		resourceJobs.getCookies().add(loginCookie);

		try {
			resourceJobs.get();
		} catch (Exception e) {
		}

		assertEquals(200, resourceJobs.getStatus().getCode());
		
		
		JSONArray result = new JSONArray(resourceJobs.getResponseEntity()
				.getText());
		return result;
		
	}
		
	}
	
	
	public CookieSetting getCookieForUser(String username, String password)
			throws IOException {
		ClientResource resourceLogin = createClientResource("/login");
		Form formStatus = new Form();
		formStatus.set("loginUsername", username);
		formStatus.set("loginPassword", password);

		resourceLogin.post(formStatus);

		System.out.println(resourceLogin.getResponseEntity().getText());

		assertEquals(200, resourceLogin.getStatus().getCode());
		assertEquals(1, resourceLogin.getResponse().getCookieSettings().size());

		return resourceLogin.getResponse().getCookieSettings().get(0);

	}

}
