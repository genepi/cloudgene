package cloudgene.mapred.api.v2.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.restlet.data.MediaType;
import org.restlet.ext.html.FormData;
import org.restlet.ext.html.FormDataSet;
import org.restlet.representation.FileRepresentation;
import org.restlet.resource.ClientResource;

import cloudgene.mapred.TestApplication;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.CloudgeneClient;
import cloudgene.mapred.util.LoginToken;
import cloudgene.mapred.util.TestCluster;
import cloudgene.mapred.util.TestSFTPServer;
import cloudgene.sdk.internal.WorkflowContext;
import genepi.io.FileUtil;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SubmitJobTest {

	@Inject
	TestApplication application;

	@Inject
	CloudgeneClient client;

	@BeforeAll
	protected void setUp() throws Exception {
		TestCluster.getInstance().start();
	}

	@Test
	public void testSubmitWithoutLogin() throws IOException, JSONException, InterruptedException {

		// form data

		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		form.getEntries().add(new FormData("input-input", "input-file"));

		// submit job
		ClientResource resource = client.createClientResource("/api/v2/jobs/submit/all-possible-inputs");
		try {
			resource.post(form);
		} catch (Exception e) {
			assertEquals(401, resource.getStatus().getCode());
		}
		resource.release();
	}

	@Test
	public void testSubmitBlockedInMaintenance() throws IOException, JSONException, InterruptedException {

		// form data

		application.getSettings().setMaintenance(true);

		// form data

		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		form.getEntries().add(new FormData("job-name", ""));
		form.getEntries().add(new FormData("input-text", "my-text"));
		form.getEntries().add(new FormData("input-number", "27"));
		// ignore checkbox
		form.getEntries().add(new FormData("input-list", "keya"));
		// local-file
		FileUtil.writeStringBufferToFile("test.txt", new StringBuffer("content-of-my-file"));
		form.getEntries().add(new FormData("input-file", new FileRepresentation("test.txt", MediaType.TEXT_PLAIN)));

		// local-folder
		FileUtil.writeStringBufferToFile("test1.txt", new StringBuffer("content-of-my-file-in-folder1"));
		FileUtil.writeStringBufferToFile("test2.txt", new StringBuffer("content-of-my-file-in-folder2"));

		form.getEntries().add(new FormData("input-folder", new FileRepresentation("test1.txt", MediaType.TEXT_PLAIN)));
		form.getEntries().add(new FormData("input-folder", new FileRepresentation("test2.txt", MediaType.TEXT_PLAIN)));

		// submit job
		LoginToken token = client.loginAsPublicUser();
		ClientResource resource = client.createClientResource("/api/v2/jobs/submit/all-possible-inputs", token);
		try {
			resource.post(form);
		} catch (Exception e) {
			assertEquals(503, resource.getStatus().getCode());
		}

		application.getSettings().setMaintenance(false);

		resource.release();
	}

	@Test
	public void testSubmitWrongApplication() throws IOException, JSONException, InterruptedException {

		// form data

		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		form.getEntries().add(new FormData("input-input", "input-file"));

		// submit job
		LoginToken token = client.loginAsPublicUser();
		ClientResource resource = client.createClientResource("/api/v2/jobs/submit/wrong-application", token);
		try {
			resource.post(form);
		} catch (Exception e) {
			assertEquals(404, resource.getStatus().getCode());
		}
		resource.release();
	}

	@Test
	public void testSubmitAllPossibleInputs() throws IOException, JSONException, InterruptedException {

		// form data

		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		form.getEntries().add(new FormData("job-name", ""));
		form.getEntries().add(new FormData("input-text", "my-text"));
		form.getEntries().add(new FormData("input-number", "27"));
		// ignore checkbox
		form.getEntries().add(new FormData("input-list", "keya"));
		// local-file
		FileUtil.writeStringBufferToFile("test.txt", new StringBuffer("content-of-my-file"));
		form.getEntries().add(new FormData("input-file", new FileRepresentation("test.txt", MediaType.TEXT_PLAIN)));

		// local-folder
		FileUtil.writeStringBufferToFile("test1.txt", new StringBuffer("content-of-my-file-in-folder1"));
		FileUtil.writeStringBufferToFile("test2.txt", new StringBuffer("content-of-my-file-in-folder2"));

		form.getEntries().add(new FormData("input-folder", new FileRepresentation("test1.txt", MediaType.TEXT_PLAIN)));
		form.getEntries().add(new FormData("input-folder", new FileRepresentation("test2.txt", MediaType.TEXT_PLAIN)));

		// submit job
		String id = client.submitJobPublic("all-possible-inputs", form);

		// check feedback
		client.waitForJob(id);

		JSONObject result = client.getJobDetails(id);
		System.out.println(result.get("name"));

		assertEquals(AbstractJob.STATE_SUCCESS, result.get("state"));

	}

	@Test
	public void testSubmitAllPossibleInputsHdfs() throws IOException, JSONException, InterruptedException {

		// form data

		FormDataSet form = new FormDataSet();
		form.setMultipart(true);

		// hdfs-file
		FileUtil.writeStringBufferToFile("test.txt", new StringBuffer("content-of-my-file"));
		form.getEntries().add(new FormData("input-file", new FileRepresentation("test.txt", MediaType.TEXT_PLAIN)));

		// hdfs-folder
		FileUtil.writeStringBufferToFile("test1.txt", new StringBuffer("content-of-my-file-in-folder1"));
		FileUtil.writeStringBufferToFile("test2.txt", new StringBuffer("content-of-my-file-in-folder2"));

		form.getEntries().add(new FormData("input-folder", new FileRepresentation("test1.txt", MediaType.TEXT_PLAIN)));
		form.getEntries().add(new FormData("input-folder", new FileRepresentation("test2.txt", MediaType.TEXT_PLAIN)));

		// submit job
		String id = client.submitJobPublic("all-possible-inputs-hdfs", form);

		// check feedback
		client.waitForJob(id);

		JSONObject result = client.getJobDetails(id);

		assertEquals(AbstractJob.STATE_SUCCESS, result.get("state"));

	}

	@Test
	public void testSubmitReturnTrueStepPublic() throws IOException, JSONException, InterruptedException {

		// form data

		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		form.getEntries().add(new FormData("input-input", "input-file"));

		// submit job
		String id = client.submitJobPublic("return-true-step-public", form);

		// check feedback
		client.waitForJob(id);

		JSONObject result = client.getJobDetails(id);

		assertEquals(AbstractJob.STATE_SUCCESS, result.get("state"));

	}

	@Test
	public void testSubmitReturnFalseStepPublic() throws IOException, JSONException, InterruptedException {

		// form data

		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		form.getEntries().add(new FormData("input-input", "input-file"));

		// submit job
		String id = client.submitJobPublic("return-false-step-public", form);

		// check feedback
		client.waitForJob(id);

		JSONObject result = client.getJobDetails(id);

		assertEquals(AbstractJob.STATE_FAILED, result.get("state"));

	}

	@Test
	public void testSubmitReturnExceptionStepPublic() throws IOException, JSONException, InterruptedException {

		// form data

		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		form.getEntries().add(new FormData("input-input", "input-file"));

		// submit job
		String id = client.submitJobPublic("return-exception-step-public", form);

		// check feedback
		client.waitForJob(id);

		JSONObject result = client.getJobDetails(id);

		assertEquals(AbstractJob.STATE_FAILED, result.get("state"));

	}

	@Test
	public void testSubmitWriteTextToFilePublic() throws IOException, JSONException, InterruptedException {

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
		String path = result.getJSONArray("outputParams").getJSONObject(0).getJSONArray("files").getJSONObject(0)
				.getString("path");

		String content = client.downloadResults(path);

		assertEquals("lukas_text", content);

	}

	@Test
	public void testSubmitWriteTextToHdfsFilePublic() throws IOException, JSONException, InterruptedException {

		// form data

		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		form.add("input-inputtext", "lukas_text");

		// submit job
		String id = client.submitJobPublic("write-text-to-hdfs-file", form);

		// check feedback
		client.waitForJob(id);

		// TODO: change!
		Thread.sleep(5000);

		// get details
		JSONObject result = client.getJobDetails(id);

		assertEquals(AbstractJob.STATE_SUCCESS, result.get("state"));

		// get path and download file
		String path = result.getJSONArray("outputParams").getJSONObject(0).getJSONArray("files").getJSONObject(0)
				.getString("path");

		String content = client.downloadResults(path);

		assertEquals("lukas_text", content);

	}

	@Test
	public void testSubmitThreeTasksStepPublic() throws IOException, JSONException, InterruptedException {

		// form data

		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		form.getEntries().add(new FormData("input-input", "input-file"));

		// submit job
		String id = client.submitJobPublic("three-tasks", form);

		// check feedback
		client.waitForJob(id);

		JSONObject result = client.getJobDetails(id);

		assertEquals(AbstractJob.STATE_SUCCESS, result.get("state"));

		JSONArray messages = result.getJSONArray("steps").getJSONObject(0).getJSONArray("logMessages");

		assertEquals(3, messages.length());
		assertEquals("cloudgene-task1", messages.getJSONObject(0).get("message"));
		assertEquals(WorkflowContext.OK, messages.getJSONObject(0).get("type"));
		assertEquals("cloudgene-task2", messages.getJSONObject(1).get("message"));
		assertEquals(WorkflowContext.OK, messages.getJSONObject(1).get("type"));
		assertEquals("cloudgene-task3", messages.getJSONObject(2).get("message"));
		assertEquals(WorkflowContext.OK, messages.getJSONObject(2).get("type"));

	}

	@Test
	public void testSubmitWithHiddenInputs() throws IOException, JSONException, InterruptedException {

		// form data

		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		// add visible checkbox
		form.getEntries().add(new FormData("input-checkbox1", "true"));

		// submit job
		String id = client.submitJobPublic("print-hidden-inputs", form);

		// check feedback
		client.waitForJob(id);

		JSONObject result = client.getJobDetails(id);

		assertEquals(AbstractJob.STATE_SUCCESS, result.get("state"));

		assertEquals(6, result.getJSONArray("steps").length());
		assertEquals("text1: my-value\n", result.getJSONArray("steps").getJSONObject(0).getJSONArray("logMessages")
				.getJSONObject(0).get("message"));
		assertEquals("checkbox1: true\n", result.getJSONArray("steps").getJSONObject(1).getJSONArray("logMessages")
				.getJSONObject(0).get("message"));
		assertEquals("list1: value1\n", result.getJSONArray("steps").getJSONObject(2).getJSONArray("logMessages")
				.getJSONObject(0).get("message"));
		assertEquals("text2: my-value\n", result.getJSONArray("steps").getJSONObject(3).getJSONArray("logMessages")
				.getJSONObject(0).get("message"));
		assertEquals("checkbox2: true\n", result.getJSONArray("steps").getJSONObject(4).getJSONArray("logMessages")
				.getJSONObject(0).get("message"));
		assertEquals("list2: value1\n", result.getJSONArray("steps").getJSONObject(5).getJSONArray("logMessages")
				.getJSONObject(0).get("message"));

	}

	@Test
	public void testSubmitSftpUpload() throws IOException, JSONException, InterruptedException {

		TestSFTPServer sftp = new TestSFTPServer("test-data");

		String url = "sftp://localhost:8001/" + new File("test-data/sftp-import.yaml").getAbsolutePath() + ";"
				+ TestSFTPServer.USERNAME + ";" + TestSFTPServer.PASSWORD;

		// form data

		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		form.getEntries().add(new FormData("input-input", url));

		// submit job
		String id = client.submitJobPublic("sftp-import", form);

		// get details to check *** bug
		client.getJobDetails(id);

		// check feedback
		client.waitForJob(id);

		JSONObject result = client.getJobDetails(id);

		// check if no sftp url is in json
		assertFalse(result.toString().contains(url));

		// get log file

		assertEquals(AbstractJob.STATE_SUCCESS, result.get("state"));

		sftp.stop();

		// check results!

	}

	// TODO: wrong permissions

	// TODO: wrong id

}
