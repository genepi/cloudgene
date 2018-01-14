package cloudgene.mapred.jobs;

import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.util.junit.TestServer;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlReader;

public class WrongWorkspaceTest extends TestCase {

	private WorkflowEngine engine;

	@Override
	protected void setUp() throws Exception {
		engine = TestServer.getInstance().startWorkflowEngineWithoutServer();

	}

	public void testReturnTrueStep() throws Exception {

		WdlApp app = WdlReader.loadAppFromFile("test-data/return-true.yaml");

		Map<String, String> inputs = new HashMap<String, String>();
		inputs.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, inputs);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(1000);
		}
		Thread.sleep(10000);

		JobDao dao = new JobDao(TestServer.getInstance().getDatabase());

		AbstractJob jobFromDb = dao.findById(job.getId());

		assertEquals(AbstractJob.STATE_FAILED, jobFromDb.getState());

		assertEquals(AbstractJob.STATE_FAILED, job.getState());
	}

	public CloudgeneJob createJobFromWdl(WdlApp app, Map<String, String> inputs) throws Exception {

		User user = TestServer.getInstance().getUser();
		Settings settings = TestServer.getInstance().getSettings();

		String id = "test_" + System.currentTimeMillis();

		String hdfsWorkspace = HdfsUtil.path("/gsfgdfgdf/vdadsadwa", id);
		String localWorkspace = FileUtil.path("/gsfgdfgdf/vdadsadwa", id);
		FileUtil.createDirectory(localWorkspace);

		CloudgeneJob job = new CloudgeneJob(user, id, app, inputs);
		job.setId(id);
		job.setName(id);
		job.setLocalWorkspace(localWorkspace);
		job.setHdfsWorkspace(hdfsWorkspace);
		job.setSettings(settings);
		job.setRemoveHdfsWorkspace(true);
		job.setApplication(app.getName() + " " + app.getVersion());
		job.setApplicationId(app.getId());

		return job;
	}

}
