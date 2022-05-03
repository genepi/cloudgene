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

		JSONObject file1 = ouput.getJSONArray("files").getJSONObject(0);
		String path1 = file1.getString("path");
		assertEquals(id + "/output/output", path1);
		String content1 = client.download(id, file1);
		assertEquals("lukas_text", content1);

		// check if it returns 404
		String randomPath = id + "/hash/lukas.txt";
		ClientResource resource = client.createClientResource("/downloads/" + randomPath, token);
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

		JSONObject file1 = ouput.getJSONArray("files").getJSONObject(0);
		String path1 = file1.getString("path");
		assertEquals(id + "/output/file1.txt", path1);
		String content1 = client.download(id, file1);
		assertEquals("lukas_text", content1);

		JSONObject file2 = ouput.getJSONArray("files").getJSONObject(1);
		String path2 = file2.getString("path");
		assertEquals(id + "/output/file2.txt", path2);
		String content2 = client.download(id, file2);
		assertEquals("lukas_text", content2);

		JSONObject file3 = ouput.getJSONArray("files").getJSONObject(2);
		String path3 = file3.getString("path");
		assertEquals(id + "/output/file3.txt", path3);
		String content3 = client.download(id, file3);
		assertEquals("lukas_text", content3);

		JSONObject file4 = ouput.getJSONArray("files").getJSONObject(3);
		String path4 = file4.getString("path");
		assertEquals(id + "/output/file4.txt", path4);
		String content4 = client.download(id, file4);
		assertEquals("lukas_text", content4);

		JSONObject file5 = ouput.getJSONArray("files").getJSONObject(4);
		String path5 = file5.getString("path");
		assertEquals(id + "/output/file5.txt", path5);
		String content5 = client.download(id, file5);
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

		JSONObject file1 = ouput.getJSONArray("files").getJSONObject(0);
		String path1 = file1.getString("path");
		assertEquals(id + "/output/file1.txt", path1);
		String content1 = client.download(id, file1);
		assertEquals("lukas_text", content1);

		JSONObject file2 = ouput.getJSONArray("files").getJSONObject(1);
		String path2 = file2.getString("path");
		assertEquals(id + "/output/file2.txt", path2);
		String content2 = client.download(id, file2);
		assertEquals("lukas_text", content2);

		JSONObject file3 = ouput.getJSONArray("files").getJSONObject(2);
		String path3 = file3.getString("path");
		assertEquals(id + "/output/file3.txt", path3);
		String content3 = client.download(id, file3);
		assertEquals("lukas_text", content3);

		JSONObject file4 = ouput.getJSONArray("files").getJSONObject(3);
		String path4 = file4.getString("path");
		assertEquals(id + "/output/file4.txt", path4);
		String content4 = client.download(id, file4);
		assertEquals("lukas_text", content4);

		JSONObject file5 = ouput.getJSONArray("files").getJSONObject(4);
		String path5 = file5.getString("path");
		assertEquals(id + "/output/file5.txt", path5);
		String content5 = client.download(id, file5);
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

		JSONObject file1 =  ouput.getJSONArray("files").getJSONObject(0);
		String path1 = file1.getString("path");
		assertEquals(id + "/output/file1.txt", path1);

		int maxDownloads = application.getSettings().getMaxDownloads();
		// download file max_download
		for (int i = 0; i < maxDownloads; i++) {
			String content1 = client.download(id, file1);
			assertEquals("lukas_text", content1);
		}

		// check if download is blocked
		ClientResource resource = client.createClientResource("/downloads/" + id + "/" + file1.getString("hash") + "/" + file1.getString("name"));
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
	public void testJobNotFound() throws IOException, JSONException, InterruptedException {

		String path = "job-lukas277/HASH/file1.txt";

		// check if download is blocked
		LoginToken login = client.login("admin", "admin1978");
		ClientResource resource = client.createClientResource("/downloads/" + path, login);
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
