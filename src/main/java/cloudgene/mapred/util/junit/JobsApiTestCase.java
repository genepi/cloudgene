package cloudgene.mapred.util.junit;

import java.io.IOException;

import org.apache.avro.data.Json;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.ext.html.FormDataSet;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

import junit.framework.TestCase;

public class JobsApiTestCase extends TestCase {

	public ClientResource createClientResource(String path) {

		ClientResource l = new ClientResource(TestServer.HOSTNAME + path);
		return l;
	}

	public ClientResource createClientResource(String path, LoginToken loginToken) {
		ClientResource resource = createClientResource(path);
		if (loginToken != null) {
			if (loginToken.getCookie() != null){
			resource.getCookies().add(loginToken.getCookie());
			}
			setupCrfToken(resource, loginToken.getCsrfToken());
		}
		return resource;
	}

	public void setupCrfToken(ClientResource resource, String token) {
		Series<Header> requestHeader = (Series<Header>) resource.getRequest().getAttributes()
				.get(HeaderConstants.ATTRIBUTE_HEADERS);
		if (requestHeader == null) {
			requestHeader = new Series(Header.class);
			resource.getRequest().getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS, requestHeader);
		}
		if (token != null){
		requestHeader.add("X-CSRF-Token", token);
		}
	}

	public void setupApiToken(ClientResource resource, String token) {
		Series<Header> requestHeader = (Series<Header>) resource.getRequest().getAttributes()
				.get(HeaderConstants.ATTRIBUTE_HEADERS);
		if (requestHeader == null) {
			requestHeader = new Series(Header.class);
			resource.getRequest().getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS, requestHeader);
		}
		requestHeader.add("X-Auth-Token", token);
	}
	
	public String submitJobPublic(String tool, FormDataSet form) throws JSONException, IOException {
		return submitJob(tool, form, null);
	}

	public String submitJob(String tool, FormDataSet form, LoginToken loginToken) throws JSONException, IOException {

		ClientResource resource = createClientResource("/api/v2/jobs/submit/" + tool, loginToken);
		resource.post(form);
		assertEquals(200, resource.getStatus().getCode());

		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), true);
		resource.release();
		return (String) object.get("id");
	}
	
	public String submitJobWithApiToken(String tool, FormDataSet form, String apiToken) throws JSONException, IOException {

		ClientResource resource = createClientResource("/api/v2/jobs/submit/" + tool);
		setupApiToken(resource, apiToken);
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

	public String restartJob(String id, LoginToken loginCookie) throws JSONException, IOException {

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

	public String cancelJob(String id, LoginToken loginCookie) throws JSONException, IOException {

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

	public String deleteJob(String id, LoginToken loginCookie) throws JSONException, IOException {

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

	public void waitForJob(String id, LoginToken loginCookie) throws IOException, JSONException, InterruptedException {

		ClientResource resourceStatus = createClientResource("/api/v2/jobs/" + id + "/status", loginCookie);

		resourceStatus.get();

		assertEquals(200, resourceStatus.getStatus().getCode());
		JSONObject object = new JSONObject(resourceStatus.getResponseEntity().getText());
		resourceStatus.release();

		boolean running = object.getInt("state") == 1 || object.getInt("state") == 2 || object.getInt("state") == 3;
		System.out.println(running);
		if (running) {
			Thread.sleep(500);
			waitForJob(id, loginCookie);
		}
	}

	public void waitForJobWithApiToken(String id, String token) throws IOException, JSONException, InterruptedException {

		ClientResource resourceStatus = createClientResource("/api/v2/jobs/" + id + "/status");
		setupApiToken(resourceStatus, token);
		resourceStatus.get();

		assertEquals(200, resourceStatus.getStatus().getCode());
		JSONObject object = new JSONObject(resourceStatus.getResponseEntity().getText());
		resourceStatus.release();

		boolean running = object.getInt("state") == 1 || object.getInt("state") == 2 || object.getInt("state") == 3;		if (running) {
			Thread.sleep(500);
			waitForJobWithApiToken(id, token);
		}
	}
	
	public JSONObject getJobDetails(String id) throws IOException, JSONException, InterruptedException {
		return getJobDetails(id, null);
	}

	public JSONObject getJobDetails(String id, LoginToken loginCookie)
			throws IOException, JSONException, InterruptedException {

		ClientResource resourceStatus = createClientResource("/api/v2/jobs/" + id, loginCookie);
		resourceStatus.get();

		assertEquals(200, resourceStatus.getStatus().getCode());
		JSONObject object = new JSONObject(resourceStatus.getResponseEntity().getText());
		resourceStatus.release();

		return object;

	}

	public JSONObject getJobDetailsWithApiToken(String id, String token)
			throws IOException, JSONException, InterruptedException {

		ClientResource resourceStatus = createClientResource("/api/v2/jobs/" + id);
		setupApiToken(resourceStatus, token);

		resourceStatus.get();

		assertEquals(200, resourceStatus.getStatus().getCode());
		JSONObject object = new JSONObject(resourceStatus.getResponseEntity().getText());
		resourceStatus.release();

		return object;

	}

	
	public String downloadResults(String path) throws IOException, JSONException, InterruptedException {
		return downloadResults(path, null);
	}

	public String downloadResults(String path, LoginToken loginCookie)
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
	
	public String downloadResultsWithApiToken(String path, String token)
			throws IOException, JSONException, InterruptedException {

		ClientResource resourceDownload = createClientResource("/results/" + path);
		setupApiToken(resourceDownload, token);

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

	public String downloadSharedResults(String user, String hash, String filename, LoginToken loginCookie)
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

	public String downloadURL(String url, LoginToken loginCookie)
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

	public JSONArray getJobs(LoginToken loginCookie) throws JSONException, IOException {
		{

			ClientResource resourceJobs = createClientResource("/api/v2/jobs", loginCookie);

			try {
				resourceJobs.get();
			} catch (Exception e) {
			}

			assertEquals(200, resourceJobs.getStatus().getCode());

			
			JSONObject object = new JSONObject(resourceJobs.getResponseEntity().getText());		
			JSONArray result = object.getJSONArray("data");
			resourceJobs.release();

			return result;

		}

	}
	
	public JSONArray getJobsWithApiToken(String token) throws JSONException, IOException {
		{

			ClientResource resourceJobs = createClientResource("/api/v2/jobs");
			setupApiToken(resourceJobs, token);
			
			try {
				resourceJobs.get();
			} catch (Exception e) {
			}

			assertEquals(200, resourceJobs.getStatus().getCode());

			JSONObject object = new JSONObject(resourceJobs.getResponseEntity().getText());		
			JSONArray result = object.getJSONArray("data");
			resourceJobs.release();

			return result;

		}

	}

	public LoginToken login(String username, String password) throws IOException {

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

		LoginToken loginToken = new LoginToken();
		loginToken.setCookie(cookie);
		loginToken.setCsrfToken(object.getString("csrf"));

		System.out.println("token: " + object.getString("csrf"));

		return loginToken;

	}

}
