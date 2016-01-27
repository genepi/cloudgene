package cloudgene.mapred.api.v2.jobs;

import org.json.JSONObject;
import org.restlet.ext.html.FormDataSet;

import cloudgene.mapred.api.v2.JobsApiTestCase;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.TestEnvironment;

public class CancelJobTest extends JobsApiTestCase {

	@Override
	protected void setUp() throws Exception {
		TestEnvironment.getInstance().startWebServer();

	}

	public void testCancelSleepJob() throws Exception {

		// form data

		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		form.add("input", "dummy");

		// submit job
		String id = submitJob("long-sleep", form);

		Thread.sleep(8000);

		//Handle answer!
		cancelJob(id);
		// get details
		JSONObject result = getJobDetails(id);
		assertEquals(AbstractJob.STATE_CANCELED, result.get("state"));

		
	}

	//TODO: wrong permissions
	
	//TODO: wrong id
	
}
