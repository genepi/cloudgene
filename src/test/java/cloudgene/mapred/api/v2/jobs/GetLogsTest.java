package cloudgene.mapred.api.v2.jobs;

import genepi.io.FileUtil;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.ext.html.FormData;
import org.restlet.ext.html.FormDataSet;
import org.restlet.representation.FileRepresentation;

import cloudgene.mapred.api.v2.JobsApiTestCase;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.TestCluster;
import cloudgene.mapred.util.TestServer;

public class GetLogsTest extends JobsApiTestCase {

	@Override
	protected void setUp() throws Exception {
		TestServer.getInstance().start();
		TestCluster.getInstance().start();
	}

	public void testSubmitAllPossibleInputs() throws IOException,
			JSONException, InterruptedException {

		// form data

		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		form.getEntries().add(new FormData("input-text", "my-text"));
		form.getEntries().add(new FormData("input-number", "27"));
		// ignore checkbox
		form.getEntries().add(new FormData("input-list", "valuea"));
		// local-file
		FileUtil.writeStringBufferToFile("test.txt", new StringBuffer(
				"content-of-my-file"));
		form.getEntries().add(
				new FormData("input-file", new FileRepresentation("test.txt",
						MediaType.TEXT_PLAIN)));

		// local-folder
		FileUtil.writeStringBufferToFile("test1.txt", new StringBuffer(
				"content-of-my-file-in-folder1"));
		FileUtil.writeStringBufferToFile("test2.txt", new StringBuffer(
				"content-of-my-file-in-folder2"));

		form.getEntries().add(
				new FormData("input-folder", new FileRepresentation(
						"test1.txt", MediaType.TEXT_PLAIN)));
		form.getEntries().add(
				new FormData("input-folder", new FileRepresentation(
						"test2.txt", MediaType.TEXT_PLAIN)));

		// submit job
		String id = submitJob("all-possible-inputs", form);

		// check feedback
		waitForJob(id);

		JSONObject result = getJobDetails(id);

		assertEquals(AbstractJob.STATE_SUCCESS, result.get("state"));

		// in public mode logs should be empty (used for links etc..)
		String logs = result.getString("logs");
		assertEquals("", logs);

		// but direct link should work
		String content = downloadURL("/logs/" + id);

		// check content for some success messages
		assertTrue(content.contains("Cleanup successful."));
		assertTrue(content.contains("Data export successful."));
		assertTrue(content.contains("Job executed successful."));
		assertTrue(content.contains("Input-Text: my-text"));
		assertTrue(content.contains("Input-number: 27"));
		assertTrue(content.contains("Input Checkbox: valueFalse"));
		assertTrue(content.contains("Input List: valuea"));

		assertTrue(content.contains("std.out:"));
		assertTrue(content.contains("Planner: WDL evaluated."));
		assertTrue(content.contains("WriteTextToFileStep"));

	}

	// TODO: wrong permissions

	// TODO: wrong id

}
