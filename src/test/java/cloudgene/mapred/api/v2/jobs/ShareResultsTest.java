package cloudgene.mapred.api.v2.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.restlet.ext.html.FormDataSet;
import org.restlet.resource.ClientResource;

import cloudgene.mapred.TestApplication;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.CloudgeneClient;
import cloudgene.mapred.util.HashUtil;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;

@MicronautTest
public class ShareResultsTest {

	@Inject
	TestApplication application;

	@Inject
	CloudgeneClient client;

	@Test
	public void testShareSingleFile() throws IOException, JSONException, InterruptedException {

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
		String hash1 = ouput.getJSONArray("files").getJSONObject(0).getString("hash");
		String filename = ouput.getJSONArray("files").getJSONObject(0).getString("name");
		assertEquals(id + "/output/output", path1);
		String content1 = client.downloadSharedResults(hash1, filename);
		assertEquals("lukas_text", content1);

		// check if it returns 404
		String randomHash = HashUtil.getSha256("random-text");
		ClientResource resource = client.createClientResource("/share/results/" + randomHash + "/output");
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
	public void testShareSingleFolder() throws IOException, JSONException, InterruptedException {

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
		String hash1 = ouput.getJSONArray("files").getJSONObject(0).getString("hash");
		String filename1 = ouput.getJSONArray("files").getJSONObject(0).getString("name");
		assertEquals(id + "/output/file1.txt", path1);
		String content1 = client.downloadSharedResults(hash1, filename1);
		assertEquals("lukas_text", content1);

		String path2 = ouput.getJSONArray("files").getJSONObject(1).getString("path");
		String hash2 = ouput.getJSONArray("files").getJSONObject(1).getString("hash");
		String filename2 = ouput.getJSONArray("files").getJSONObject(1).getString("name");
		assertEquals(id + "/output/file2.txt", path2);
		String content2 = client.downloadSharedResults(hash2, filename2);
		assertEquals("lukas_text", content2);

		String path3 = ouput.getJSONArray("files").getJSONObject(2).getString("path");
		String hash3 = ouput.getJSONArray("files").getJSONObject(2).getString("hash");
		String filename3 = ouput.getJSONArray("files").getJSONObject(2).getString("name");
		assertEquals(id + "/output/file3.txt", path3);
		String content3 = client.downloadSharedResults(hash3, filename3);
		assertEquals("lukas_text", content3);

		String path4 = ouput.getJSONArray("files").getJSONObject(3).getString("path");
		String hash4 = ouput.getJSONArray("files").getJSONObject(3).getString("hash");
		String filename4 = ouput.getJSONArray("files").getJSONObject(3).getString("name");
		assertEquals(id + "/output/file4.txt", path4);
		String content4 = client.downloadSharedResults(hash4, filename4);
		assertEquals("lukas_text", content4);

		String path5 = ouput.getJSONArray("files").getJSONObject(4).getString("path");
		String hash5 = ouput.getJSONArray("files").getJSONObject(4).getString("hash");
		String filename5 = ouput.getJSONArray("files").getJSONObject(4).getString("name");
		assertEquals(id + "/output/file5.txt", path5);
		String content5 = client.downloadSharedResults(hash5, filename5);
		assertEquals("lukas_text", content5);

	}

	@Test
	public void testShareCounter() throws IOException, JSONException, InterruptedException {

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
		String hash1 = ouput.getJSONArray("files").getJSONObject(0).getString("hash");
		String filename1 = ouput.getJSONArray("files").getJSONObject(0).getString("name");
		assertEquals(id + "/output/file1.txt", path1);

		// download file max_download
		int maxDownloads = application.getSettings().getMaxDownloads();
		for (int i = 0; i < maxDownloads; i++) {
			String content1 = client.downloadSharedResults(hash1, filename1);
			assertEquals("lukas_text", content1);
		}

		// check if download is blocked
		ClientResource resource = client.createClientResource("/share/results/" + hash1 + "/" + filename1);
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
}
