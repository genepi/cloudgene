package cloudgene.mapred.util.junit;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.Protocol;
import org.restlet.ext.html.FormDataSet;
import org.restlet.resource.ClientResource;

import junit.framework.TestCase;

public class JobsApiTestCase extends TestCase {

	public ClientResource createClientResource(String path) {

		ClientResource l = new ClientResource(TestServer.HOSTNAME + path);
		return l;
	}

	public ClientResource createClientResource(String path, CookieSetting loginCookie) {
		ClientResource resource = createClientResource(path);
		if (loginCookie != null) {
			resource.getCookies().add(loginCookie);
		}
		return resource;
	}

	public String submitJob(String tool, FormDataSet form) throws JSONException, IOException {
		return submitJob(tool, form, null);
	}

	public String submitJob(String tool, FormDataSet form, CookieSetting loginCookie)
			throws JSONException, IOException {

		ClientResource resource = createClientResource("/api/v2/jobs/submit/" + tool, loginCookie);
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());

		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), true);
		resource.release();
		return (String) object.get("id");
	}

	public String restartJob(String id) throws JSONException, IOException {
		return restartJob(id, null);
	}

	public String restartJob(String id, CookieSetting loginCookie) throws JSONException, IOException {

		ClientResource resource = createClientResource("/api/v2/jobs/" + id + "/restart", loginCookie);

		resource.get();
		assertEquals(200, resource.getStatus().getCode());

		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), true);
		resource.release();
		return (String) object.get("id");
	}

	public String cancelJob(String id) throws JSONException, IOException {
		return cancelJob(id, null);
	}

	public String cancelJob(String id, CookieSetting loginCookie) throws JSONException, IOException {

		ClientResource resource = createClientResource("/api/v2/jobs/" + id + "/cancel", loginCookie);
		resource.get();
		assertEquals(200, resource.getStatus().getCode());

		// JSONObject object = new JSONObject(resource.getResponseEntity()
		// .getText());
		// assertEquals(object.get("success"), true);
		resource.release();
		return "";
	}

	public String deleteJob(String id) throws JSONException, IOException {
		return deleteJob(id, null);
	}

	public String deleteJob(String id, CookieSetting loginCookie) throws JSONException, IOException {

		ClientResource resource = createClientResource("/api/v2/jobs/" + id, loginCookie);

		resource.delete();
		assertEquals(200, resource.getStatus().getCode());

		// JSONObject object = new JSONObject(resource.getResponseEntity()
		// .getText());
		// assertEquals(object.get("success"), true);
		resource.release();
		return "";
	}

	public void waitForJob(String id) throws IOException, JSONException, InterruptedException {
		waitForJob(id, null);
	}

	public void waitForJob(String id, CookieSetting loginCookie)
			throws IOException, JSONException, InterruptedException {

		ClientResource resourceStatus = createClientResource("/api/v2/jobs/" + id + "/status", loginCookie);

		resourceStatus.get();

		assertEquals(200, resourceStatus.getStatus().getCode());
		JSONObject object = new JSONObject(resourceStatus.getResponseEntity().getText());
		resourceStatus.release();

		boolean running = object.getBoolean("running");
		if (running) {
			Thread.sleep(1000);
			waitForJob(id, loginCookie);
		}
	}

	public JSONObject getJobDetails(String id) throws IOException, JSONException, InterruptedException {
		return getJobDetails(id, null);
	}

	public JSONObject getJobDetails(String id, CookieSetting loginCookie)
			throws IOException, JSONException, InterruptedException {

		ClientResource resourceStatus = createClientResource("/api/v2/jobs/" + id, loginCookie);
		resourceStatus.get();

		assertEquals(200, resourceStatus.getStatus().getCode());
		JSONObject object = new JSONObject(resourceStatus.getResponseEntity().getText());
		resourceStatus.release();

		return object;

	}

	public String downloadResults(String path) throws IOException, JSONException, InterruptedException {
		return downloadResults(path, null);
	}

	public String downloadResults(String path, CookieSetting loginCookie)
			throws IOException, JSONException, InterruptedException {

		ClientResource resourceDownload = createClientResource("/results/" + path, loginCookie);

		try {
			resourceDownload.get();
			assertEquals(200, resourceDownload.getStatus().getCode());
			String text = resourceDownload.getResponseEntity().getText();
			resourceDownload.release();
			return text;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(resourceDownload.getResponseEntity().getText());
			assertEquals(false, true);
			return null;
		}

	}

	public String downloadSharedResults(String user, String hash, String filename)
			throws IOException, JSONException, InterruptedException {
		return downloadSharedResults(user, hash, filename, null);
	}

	public String downloadSharedResults(String user, String hash, String filename, CookieSetting loginCookie)
			throws IOException, JSONException, InterruptedException {

		ClientResource resourceDownload = createClientResource("/share/" + user + "/" + hash + "/" + filename,
				loginCookie);

		try {
			resourceDownload.get();
			assertEquals(200, resourceDownload.getStatus().getCode());
			String text = resourceDownload.getResponseEntity().getText();
			resourceDownload.release();
			return text;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(resourceDownload.getResponseEntity().getText());
			assertEquals(false, true);
			return null;
		}

	}

	public String downloadURL(String url) throws IOException, JSONException, InterruptedException {
		return downloadURL(url, null);
	}

	public String downloadURL(String url, CookieSetting loginCookie)
			throws IOException, JSONException, InterruptedException {

		ClientResource resourceDownload = createClientResource(url, loginCookie);

		try {
			resourceDownload.get();
			assertEquals(200, resourceDownload.getStatus().getCode());
			String text = resourceDownload.getResponseEntity().getText();
			resourceDownload.release();
			return text;
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(false, true);
			return null;
		}

	}

	public JSONArray getJobs(CookieSetting loginCookie) throws JSONException, IOException {
		{

			ClientResource resourceJobs = createClientResource("/api/v2/jobs");
			resourceJobs.getCookies().add(loginCookie);

			try {
				resourceJobs.get();
			} catch (Exception e) {
			}

			assertEquals(200, resourceJobs.getStatus().getCode());

			JSONArray result = new JSONArray(resourceJobs.getResponseEntity().getText());
			resourceJobs.release();

			return result;

		}

	}

	public CookieSetting getCookieForUser(String username, String password) throws IOException {
		ClientResource resourceLogin = createClientResource("/login");
		Form formStatus = new Form();
		formStatus.set("loginUsername", username);
		formStatus.set("loginPassword", password);

		resourceLogin.post(formStatus);

		assertEquals(200, resourceLogin.getStatus().getCode());
		JSONObject object = new JSONObject(resourceLogin.getResponseEntity().getText());
		assertEquals("Login successfull.", object.getString("message"));
		assertEquals(true, object.get("success"));
		assertEquals(1, resourceLogin.getResponse().getCookieSettings().size());

		CookieSetting cookie = resourceLogin.getResponse().getCookieSettings().get(0);
		resourceLogin.release();

		return cookie;

	}

}
