package cloudgene.mapred.api.v2.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.ext.html.FormData;
import org.restlet.ext.html.FormDataSet;
import org.restlet.representation.FileRepresentation;

import cloudgene.mapred.TestApplication;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.CloudgeneClient;
import genepi.io.FileUtil;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;

@MicronautTest
public class GetLogsTest {

	@Inject
	TestApplication application;

	@Inject
	CloudgeneClient client;

	@Test
	public void testSubmitAllPossibleInputs() throws IOException, JSONException, InterruptedException {

		// form data

		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
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

		assertEquals(AbstractJob.STATE_SUCCESS, result.get("state"));

		// in public mode logs should be empty (used for links etc..)
		String logs = result.getString("logs");
		assertEquals("", logs);

		// but direct link should work
		String content = client.downloadURL("/logs/" + id);

		// check content for some success messages
		assertTrue(content.contains("Cleanup successful."));
		assertTrue(content.contains("Data Export successful."));
		assertTrue(content.contains("Job Execution successful."));
		assertTrue(content.contains("Input-Text: my-text"));
		assertTrue(content.contains("Input-number: 27"));
		assertTrue(content.contains("Input Checkbox: valueFalse"));
		assertTrue(content.contains("Input List: keya"));

		assertTrue(content.contains("Planner: WDL evaluated."));
		assertTrue(content.contains("CheckInputs"));

	}

	@Test
	public void testWriteToStdOuStepPublic() throws IOException, JSONException, InterruptedException {

		// form data

		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		form.getEntries().add(new FormData("input-input", "input-file"));

		// submit job
		String id = client.submitJobPublic("write-text-to-std-out", form);

		// check feedback
		client.waitForJob(id);

		JSONObject result = client.getJobDetails(id);

		assertEquals(AbstractJob.STATE_SUCCESS, result.get("state"));

		// in public mode logs should be empty (used for links etc..)
		String logs = result.getString("logs");
		assertEquals("", logs);

		// but direct link should work
		String content = client.downloadURL("/logs/" + id);

		assertTrue(content.contains("taks write to system out"));
		assertTrue(content.contains("taks write to system out2"));
		assertTrue(content.contains("taks write to system out3"));

		assertTrue(content.contains("taks write to log"));
		assertTrue(content.contains("taks write to log2"));
		assertTrue(content.contains("taks write to log3"));

	}

	// TODO: wrong permissions

	// TODO: wrong id

}
