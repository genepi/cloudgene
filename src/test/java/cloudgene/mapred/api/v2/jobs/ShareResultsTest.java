package cloudgene.mapred.api.v2.jobs;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.html.FormDataSet;
import org.restlet.resource.ClientResource;

import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.junit.JobsApiTestCase;
import cloudgene.mapred.util.junit.TestCluster;
import cloudgene.mapred.util.junit.TestServer;

public class ShareResultsTest extends JobsApiTestCase {

	public static final String username1 = "some_username"; // dummy value, only
															// needed to build
															// readable urls.

	@Override
	protected void setUp() throws Exception {
		TestServer.getInstance().start();
		TestCluster.getInstance().start();
	}

	public void testShareSingleFile() throws IOException, JSONException, InterruptedException {

		// form data

		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		form.add("input-inputtext", "lukas_text");

		// submit job
		String id = submitJobPublic("write-text-to-file", form);

		// check feedback
		waitForJob(id);

		// TODO: change!
		Thread.sleep(5000);

		// get details
		JSONObject result = getJobDetails(id);

		assertEquals(AbstractJob.STATE_SUCCESS, result.get("state"));

		// get path and download file
		JSONObject ouput = result.getJSONArray("outputParams").getJSONObject(0);
		assertEquals("output", ouput.get("name"));
		assertEquals(1, ouput.getJSONArray("files").length());

		String path1 = ouput.getJSONArray("files").getJSONObject(0).getString("path");
		String hash1 = ouput.getJSONArray("files").getJSONObject(0).getString("hash");
		String filename = ouput.getJSONArray("files").getJSONObject(0).getString("name");
		assertEquals(id + "/output/output", path1);
		String content1 = downloadSharedResults(username1, hash1, filename);
		assertEquals("lukas_text", content1);

		// check if it returns 404
		String randomHash = HashUtil.getMD5("random-text");
		ClientResource resource = createClientResource("/share/" + username1 + "/" + randomHash + "/output");
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

	public void testShareSingleFolder() throws IOException, JSONException, InterruptedException {

		// form data

		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		form.add("input-inputtext", "lukas_text");

		// submit job
		String id = submitJobPublic("write-files-to-folder", form);

		// check feedback
		waitForJob(id);

		// TODO: change!
		Thread.sleep(5000);

		// get details
		JSONObject result = getJobDetails(id);

		assertEquals(AbstractJob.STATE_SUCCESS, result.get("state"));

		// get path and download file

		JSONObject ouput = result.getJSONArray("outputParams").getJSONObject(0);
		assertEquals("output", ouput.get("name"));
		assertEquals(5, ouput.getJSONArray("files").length());

		String path1 = ouput.getJSONArray("files").getJSONObject(0).getString("path");
		String hash1 = ouput.getJSONArray("files").getJSONObject(0).getString("hash");
		String filename1 = ouput.getJSONArray("files").getJSONObject(0).getString("name");
		assertEquals(id + "/output/file1.txt", path1);
		String content1 = downloadSharedResults(username1, hash1, filename1);
		assertEquals("lukas_text", content1);

		String path2 = ouput.getJSONArray("files").getJSONObject(1).getString("path");
		String hash2 = ouput.getJSONArray("files").getJSONObject(1).getString("hash");
		String filename2 = ouput.getJSONArray("files").getJSONObject(1).getString("name");
		assertEquals(id + "/output/file2.txt", path2);
		String content2 = downloadSharedResults(username1, hash2, filename2);
		assertEquals("lukas_text", content2);

		String path3 = ouput.getJSONArray("files").getJSONObject(2).getString("path");
		String hash3 = ouput.getJSONArray("files").getJSONObject(2).getString("hash");
		String filename3 = ouput.getJSONArray("files").getJSONObject(2).getString("name");
		assertEquals(id + "/output/file3.txt", path3);
		String content3 = downloadSharedResults(username1, hash3, filename3);
		assertEquals("lukas_text", content3);

		String path4 = ouput.getJSONArray("files").getJSONObject(3).getString("path");
		String hash4 = ouput.getJSONArray("files").getJSONObject(3).getString("hash");
		String filename4 = ouput.getJSONArray("files").getJSONObject(3).getString("name");
		assertEquals(id + "/output/file4.txt", path4);
		String content4 = downloadSharedResults(username1, hash4, filename4);
		assertEquals("lukas_text", content4);

		String path5 = ouput.getJSONArray("files").getJSONObject(4).getString("path");
		String hash5 = ouput.getJSONArray("files").getJSONObject(4).getString("hash");
		String filename5 = ouput.getJSONArray("files").getJSONObject(4).getString("name");
		assertEquals(id + "/output/file5.txt", path5);
		String content5 = downloadSharedResults(username1, hash5, filename5);
		assertEquals("lukas_text", content5);

	}

	public void testShareCounter() throws IOException, JSONException, InterruptedException {

		// form data

		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		form.add("input-inputtext", "lukas_text");

		// submit job
		String id = submitJobPublic("write-files-to-folder", form);

		// check feedback
		waitForJob(id);

		// TODO: change!
		Thread.sleep(5000);

		// get details
		JSONObject result = getJobDetails(id);

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
		for (int i = 0; i < CloudgeneJob.MAX_DOWNLOAD; i++) {
			String content1 = downloadSharedResults(username1, hash1, filename1);
			assertEquals("lukas_text", content1);
		}

		// check if download is blocked
		ClientResource resource = createClientResource("/share/" + username1 + "/" + hash1 + "/" + filename1);
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
