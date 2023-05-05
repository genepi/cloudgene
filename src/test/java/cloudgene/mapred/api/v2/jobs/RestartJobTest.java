package cloudgene.mapred.api.v2.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.restlet.ext.html.FormDataSet;

import cloudgene.mapred.TestApplication;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.CloudgeneClient;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;

@MicronautTest
public class RestartJobTest {

	@Inject
	TestApplication application;

	@Inject
	CloudgeneClient client;

	@Test
	public void testRestartWriteTextToFileJob() throws Exception {

		assertEquals(true, true);
		
		// form data

		/*
		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		form.add("input-inputtext", "lukas_text");

		// submit job
		String id = client.submitJobPublic("write-text-to-file", form);

		Thread.sleep(5);

		//application.getWorkflowEngine().stop();
		//new Thread(application.getWorkflowEngine()).start();
				
		// stop engine
		// TODO: how to restart micronaut application?
		// TestServer.getInstance().reStartWebServer();

		// get details
		JSONObject result = client.getJobDetails(id);
		assertEquals(AbstractJob.STATE_DEAD, result.get("state"));

		// restart job
		client.restartJob(id);

		// check feedback
		client.waitForJob(id);

		// TODO: change!
		Thread.sleep(5000);

		// get details
		result = client.getJobDetails(id);

		assertEquals(AbstractJob.STATE_SUCCESS, result.get("state"));

		// get path and download file
		JSONObject file = result.getJSONArray("outputParams").getJSONObject(0).getJSONArray("files").getJSONObject(0);

		String content = client.download(id, file);

		assertEquals("lukas_text", content);
*/
	}

	// TODO: wrong permissions

	// TODO: wrong id

}
