package cloudgene.mapred.api.v2.jobs;

import org.json.JSONObject;
import org.restlet.ext.html.FormDataSet;

import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.junit.JobsApiTestCase;
import cloudgene.mapred.util.junit.TestCluster;
import cloudgene.mapred.util.junit.TestServer;

public class RestartJobTest extends JobsApiTestCase {

	@Override
	protected void setUp() throws Exception {
		TestServer.getInstance().start();
		TestCluster.getInstance().start();
	}

	public void testRestartWriteTextToFileJob() throws Exception {

		// form data

		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		form.add("input-inputtext", "lukas_text");

		// submit job
		String id = submitJobPublic("write-text-to-file", form);

		Thread.sleep(5);

		// stop engine
		TestServer.getInstance().reStartWebServer();

		// get details
		JSONObject result = getJobDetails(id);
		assertEquals(AbstractJob.STATE_DEAD, result.get("state"));

		// restart job
		restartJob(id);

		// check feedback
		waitForJob(id);

		// TODO: change!
		Thread.sleep(5000);
		
		// get details
		result = getJobDetails(id);

	
		assertEquals(AbstractJob.STATE_SUCCESS, result.get("state"));

		// get path and download file
		String path = result.getJSONArray("outputParams").getJSONObject(0)
				.getJSONArray("files").getJSONObject(0).getString("path");

		String content = downloadResults(path);

		assertEquals("lukas_text", content);

	}

	//TODO: wrong permissions
	
	//TODO: wrong id

}
