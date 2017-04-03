package cloudgene.mapred.api.v2.jobs;

import genepi.io.FileUtil;

import java.io.File;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.ext.html.FormDataSet;
import org.restlet.resource.ClientResource;

import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.junit.JobsApiTestCase;
import cloudgene.mapred.util.junit.TestServer;

public class DeleteJobTest extends JobsApiTestCase {

	@Override
	protected void setUp() throws Exception {
		TestServer.getInstance().start();

	}

	public void testSubmitWriteTextToFilePublic() throws IOException,
			JSONException, InterruptedException {

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
		String path = result.getJSONArray("outputParams").getJSONObject(0)
				.getJSONArray("files").getJSONObject(0).getString("path");

		String content = downloadResults(path);

		assertEquals("lukas_text", content);

		// deleteJob
		deleteJob(id);

		// check if all data are deleted
		assertFalse(new File(FileUtil.path(TestServer.getInstance()
				.getSettings().getLocalWorkspace(), id)).exists());

		// TODO: same on hdfs

		// check if job was deleted from database (return 404)
		ClientResource resourceStatus = createClientResource("/jobs/details");
		Form formStatus = new Form();
		formStatus.set("id", id);

		try {
			resourceStatus.post(formStatus);
		} catch (Exception e) {
		}
		assertEquals(404, resourceStatus.getStatus().getCode());
		resourceStatus.release();
	}

	//TODO: wrong permissions
	
	//TODO: wrong id
	
}
