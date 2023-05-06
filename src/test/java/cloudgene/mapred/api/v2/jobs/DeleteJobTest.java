package cloudgene.mapred.api.v2.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.restlet.data.Form;
import org.restlet.ext.html.FormDataSet;
import org.restlet.resource.ClientResource;

import cloudgene.mapred.TestApplication;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.CloudgeneClient;
import genepi.io.FileUtil;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;

@MicronautTest
public class DeleteJobTest {

	@Inject
	TestApplication application;

	@Inject
	CloudgeneClient client;

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
		JSONObject file = result.getJSONArray("outputParams").getJSONObject(0).getJSONArray("files").getJSONObject(0);

		String content = client.download(id, file);

		assertEquals("lukas_text", content);

		// deleteJob
		client.deleteJob(id);

		// check if all data are deleted
		assertFalse(new File(FileUtil.path(application.getSettings().getLocalWorkspace(), id)).exists());

		// TODO: same on hdfs

		// check if job was deleted from database (return 404)
		ClientResource resourceStatus = client.createClientResource("/jobs/details");
		Form formStatus = new Form();
		formStatus.set("id", id);

		try {
			resourceStatus.post(formStatus);
		} catch (Exception e) {
		}
		assertEquals(404, resourceStatus.getStatus().getCode());
		resourceStatus.release();
	}

	// TODO: wrong permissions

	// TODO: wrong id

}
