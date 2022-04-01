package cloudgene.mapred.api.v2.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.restlet.ext.html.FormDataSet;
import org.restlet.resource.ClientResource;

import cloudgene.mapred.TestApplication;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.CloudgeneClient;
import cloudgene.mapred.util.LoginToken;
import cloudgene.mapred.util.TestCluster;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DownloadResultsTest {

	@Inject
	TestApplication application;

	@Inject
	CloudgeneClient client;

	@BeforeAll
	protected void setUp() throws Exception {
		TestCluster.getInstance().start();
	}
	
	@Test
	public void testDownloadSingleFile() throws IOException, JSONException, InterruptedException {

		LoginToken token = client.loginAsPublicUser();
		
		// form data

		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		form.add("input-inputtext", "lukas_text");

		// submit job
		String id = client.submitJobPublic("write-text-to-file", form);

		// check feedback
		client.waitForJob(id);

		// TODO: change!
		Thread.sleep(5000);

		// get details
		JSONObject result = client.getJobDetails(id);

		assertEquals(AbstractJob.STATE_SUCCESS, result.get("state"));

		// get path and download file
		JSONObject ouput = result.getJSONArray("outputParams").getJSONObject(0);
		assertEquals("output", ouput.get("name"));
		assertEquals(1, ouput.getJSONArray("files").length());

		String path1 = ouput.getJSONArray("files").getJSONObject(0).getString("path");
		assertEquals(id + "/output/output", path1);
		String content1 = client.downloadResults(path1);
		assertEquals("lukas_text", content1);

		// check if it returns 404
		String randomPath = id + "/output/lukas.txt";
		ClientResource resource = client.createClientResource("/results/" + randomPath, token);
		try {
			resource.get();
		} catch (Exception e) {

		}
		assertEquals(404, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertEquals(object.get("message"), "download not found.");
		resource.release();
	}

	@Test
	public void testDownloadSingleFolder() throws IOException, JSONException, InterruptedException {

		// form data

		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		form.add("input-inputtext", "lukas_text");

		// submit job
		String id = client.submitJobPublic("write-files-to-folder", form);

		// check feedback
		client.waitForJob(id);

		// TODO: change!
		Thread.sleep(5000);

		// get details
		JSONObject result = client.getJobDetails(id);

		assertEquals(AbstractJob.STATE_SUCCESS, result.get("state"));

		// get path and download file

		JSONObject ouput = result.getJSONArray("outputParams").getJSONObject(0);
		assertEquals("output", ouput.get("name"));
		assertEquals(5, ouput.getJSONArray("files").length());

		String path1 = ouput.getJSONArray("files").getJSONObject(0).getString("path");
		assertEquals(id + "/output/file1.txt", path1);
		String content1 = client.downloadResults(path1);
		assertEquals("lukas_text", content1);

		String path2 = ouput.getJSONArray("files").getJSONObject(1).getString("path");
		assertEquals(id + "/output/file2.txt", path2);
		String content2 = client.downloadResults(path2);
		assertEquals("lukas_text", content2);

		String path3 = ouput.getJSONArray("files").getJSONObject(2).getString("path");
		assertEquals(id + "/output/file3.txt", path3);
		String content3 = client.downloadResults(path3);
		assertEquals("lukas_text", content3);

		String path4 = ouput.getJSONArray("files").getJSONObject(3).getString("path");
		assertEquals(id + "/output/file4.txt", path4);
		String content4 = client.downloadResults(path4);
		assertEquals("lukas_text", content4);

		String path5 = ouput.getJSONArray("files").getJSONObject(4).getString("path");
		assertEquals(id + "/output/file5.txt", path5);
		String content5 = client.downloadResults(path5);
		assertEquals("lukas_text", content5);

	}

	@Test
	public void testDownloadSingleHdfsFolder() throws IOException, JSONException, InterruptedException {

		// form data

		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		form.add("input-inputtext", "lukas_text");

		// submit job
		String id = client.submitJobPublic("write-files-to-hdfs-folder", form);

		// check feedback
		client.waitForJob(id);

		// TODO: change!
		Thread.sleep(5000);

		// get details
		JSONObject result = client.getJobDetails(id);

		assertEquals(AbstractJob.STATE_SUCCESS, result.get("state"));

		// get path and download file

		JSONObject ouput = result.getJSONArray("outputParams").getJSONObject(0);
		assertEquals("output", ouput.get("name"));
		assertEquals(5, ouput.getJSONArray("files").length());

		String path1 = ouput.getJSONArray("files").getJSONObject(0).getString("path");
		assertEquals(id + "/output/file1.txt", path1);
		String content1 = client.downloadResults(path1);
		assertEquals("lukas_text", content1);

		String path2 = ouput.getJSONArray("files").getJSONObject(1).getString("path");
		assertEquals(id + "/output/file2.txt", path2);
		String content2 = client.downloadResults(path2);
		assertEquals("lukas_text", content2);

		String path3 = ouput.getJSONArray("files").getJSONObject(2).getString("path");
		assertEquals(id + "/output/file3.txt", path3);
		String content3 = client.downloadResults(path3);
		assertEquals("lukas_text", content3);

		String path4 = ouput.getJSONArray("files").getJSONObject(3).getString("path");
		assertEquals(id + "/output/file4.txt", path4);
		String content4 = client.downloadResults(path4);
		assertEquals("lukas_text", content4);

		String path5 = ouput.getJSONArray("files").getJSONObject(4).getString("path");
		assertEquals(id + "/output/file5.txt", path5);
		String content5 = client.downloadResults(path5);
		assertEquals("lukas_text", content5);

	}

	@Test
	public void testDownloadCounter() throws IOException, JSONException, InterruptedException {

		// form data

		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		form.add("input-inputtext", "lukas_text");

		// submit job
		String id = client.submitJobPublic("write-files-to-folder", form);

		// check feedback
		client.waitForJob(id);

		// TODO: change!
		Thread.sleep(5000);

		// get details
		JSONObject result = client.getJobDetails(id);

		assertEquals(AbstractJob.STATE_SUCCESS, result.get("state"));

		// get path and download file

		JSONObject ouput = result.getJSONArray("outputParams").getJSONObject(0);
		assertEquals("output", ouput.get("name"));
		assertEquals(5, ouput.getJSONArray("files").length());

		String path1 = ouput.getJSONArray("files").getJSONObject(0).getString("path");
		assertEquals(id + "/output/file1.txt", path1);

		int maxDownloads = application.getSettings().getMaxDownloads();
		// download file max_download
		for (int i = 0; i < maxDownloads; i++) {
			String content1 = client.downloadResults(path1);
			assertEquals("lukas_text", content1);
		}

		// check if download is blocked
		LoginToken token = client.loginAsPublicUser();
		ClientResource resource = client.createClientResource("/results/" + path1, token);
		try {
			resource.get();
		} catch (Exception e) {

		}
		assertEquals(400, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertEquals(object.get("message"), "number of max downloads exceeded.");
		resource.release();
	}

	@Test
	public void testJobWithoutLogin() throws IOException, JSONException, InterruptedException {

		String path = "job-lukas277/output/file1.txt";

		// check if download is blocked
		ClientResource resource = client.createClientResource("/results/" + path);
		try {
			resource.get();
		} catch (Exception e) {

		}
		assertEquals(401, resource.getStatus().getCode());
		resource.release();
	}
	
	@Test
	public void testJobNotFound() throws IOException, JSONException, InterruptedException {

		String path = "job-lukas277/output/file1.txt";

		// check if download is blocked
		LoginToken login = client.login("admin", "admin1978");
		ClientResource resource = client.createClientResource("/results/" + path, login);
		try {
			resource.get();
		} catch (Exception e) {

		}
		assertEquals(404, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity().getText());
		assertEquals(object.get("success"), false);
		assertEquals(object.get("message"), "Job job-lukas277 not found.");
		resource.release();
	}

}
