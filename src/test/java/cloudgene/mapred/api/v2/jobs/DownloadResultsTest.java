package cloudgene.mapred.api.v2.jobs;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.html.FormDataSet;
import org.restlet.resource.ClientResource;

import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.util.junit.JobsApiTestCase;
import cloudgene.mapred.util.junit.TestCluster;
import cloudgene.mapred.util.junit.TestServer;

public class DownloadResultsTest extends JobsApiTestCase {

	@Override
	protected void setUp() throws Exception {
		TestCluster.getInstance().start();
		TestServer.getInstance().start();
	}

	public void testDownloadSingleFile() throws IOException, JSONException,
			InterruptedException {

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

		String path1 = ouput.getJSONArray("files").getJSONObject(0)
				.getString("path");
		assertEquals(id + "/output/output", path1);
		String content1 = downloadResults(path1);
		assertEquals("lukas_text", content1);

		// check if it returns 404
		String randomPath = id + "/output/lukas.txt";
		ClientResource resource = createClientResource("/results/" + randomPath);
		try {
			resource.get();
		} catch (Exception e) {

		}
		assertEquals(404, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity()
				.getText());
		assertEquals(object.get("success"), false);
		assertEquals(object.get("message"), "download not found.");
		resource.release();
	}

	public void testDownloadSingleFolder() throws IOException, JSONException,
			InterruptedException {

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

		String path1 = ouput.getJSONArray("files").getJSONObject(0)
				.getString("path");
		assertEquals(id + "/output/file1.txt", path1);
		String content1 = downloadResults(path1);
		assertEquals("lukas_text", content1);

		String path2 = ouput.getJSONArray("files").getJSONObject(1)
				.getString("path");
		assertEquals(id + "/output/file2.txt", path2);
		String content2 = downloadResults(path2);
		assertEquals("lukas_text", content2);

		String path3 = ouput.getJSONArray("files").getJSONObject(2)
				.getString("path");
		assertEquals(id + "/output/file3.txt", path3);
		String content3 = downloadResults(path3);
		assertEquals("lukas_text", content3);

		String path4 = ouput.getJSONArray("files").getJSONObject(3)
				.getString("path");
		assertEquals(id + "/output/file4.txt", path4);
		String content4 = downloadResults(path4);
		assertEquals("lukas_text", content4);

		String path5 = ouput.getJSONArray("files").getJSONObject(4)
				.getString("path");
		assertEquals(id + "/output/file5.txt", path5);
		String content5 = downloadResults(path5);
		assertEquals("lukas_text", content5);

	}

	public void testDownloadSingleHdfsFolder() throws IOException,
			JSONException, InterruptedException {

		// form data

		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		form.add("input-inputtext", "lukas_text");

		// submit job
		String id = submitJobPublic("write-files-to-hdfs-folder", form);

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

		String path1 = ouput.getJSONArray("files").getJSONObject(0)
				.getString("path");
		assertEquals(id + "/output/file1.txt", path1);
		String content1 = downloadResults(path1);
		assertEquals("lukas_text", content1);

		String path2 = ouput.getJSONArray("files").getJSONObject(1)
				.getString("path");
		assertEquals(id + "/output/file2.txt", path2);
		String content2 = downloadResults(path2);
		assertEquals("lukas_text", content2);

		String path3 = ouput.getJSONArray("files").getJSONObject(2)
				.getString("path");
		assertEquals(id + "/output/file3.txt", path3);
		String content3 = downloadResults(path3);
		assertEquals("lukas_text", content3);

		String path4 = ouput.getJSONArray("files").getJSONObject(3)
				.getString("path");
		assertEquals(id + "/output/file4.txt", path4);
		String content4 = downloadResults(path4);
		assertEquals("lukas_text", content4);

		String path5 = ouput.getJSONArray("files").getJSONObject(4)
				.getString("path");
		assertEquals(id + "/output/file5.txt", path5);
		String content5 = downloadResults(path5);
		assertEquals("lukas_text", content5);

	}

	public void testDownloadCounter() throws IOException, JSONException,
			InterruptedException {

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

		String path1 = ouput.getJSONArray("files").getJSONObject(0)
				.getString("path");
		assertEquals(id + "/output/file1.txt", path1);

		// download file max_download
		for (int i = 0; i < CloudgeneJob.MAX_DOWNLOAD; i++) {
			String content1 = downloadResults(path1);
			assertEquals("lukas_text", content1);
		}

		// check if download is blocked
		ClientResource resource = createClientResource("/results/" + path1);
		try {
			resource.get();
		} catch (Exception e) {

		}
		assertEquals(400, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity()
				.getText());
		assertEquals(object.get("success"), false);
		assertEquals(object.get("message"), "number of max downloads exceeded.");
		resource.release();
	}

	public void testJobNotFound() throws IOException, JSONException,
			InterruptedException {

		String path = "job-lukas277/output/file1.txt";

		// check if download is blocked
		ClientResource resource = createClientResource("/results/" + path);
		try {
			resource.get();
		} catch (Exception e) {

		}
		assertEquals(404, resource.getStatus().getCode());
		JSONObject object = new JSONObject(resource.getResponseEntity()
				.getText());
		assertEquals(object.get("success"), false);
		assertEquals(object.get("message"), "Job job-lukas277 not found.");
		resource.release();
	}

}
