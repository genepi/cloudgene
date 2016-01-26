package cloudgene.mapred.jobs;

import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import cloudgene.mapred.core.User;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.util.TestEnvironment;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlReader;

public class WorkflowEngineTest extends TestCase {

	private WorkflowEngine engine;

	@Override
	protected void setUp() throws Exception {
		engine = TestEnvironment.getInstance()
				.startWorkflowEngineWithoutServer();

	}

	public void testReturnTrueStep() throws Exception {

		WdlApp app = WdlReader.loadAppFromFile("test-data/return-true.yaml");

		Map<String, String> inputs = new HashMap<String, String>();
		inputs.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, inputs);
		engine.submit(job);
		while (job.isRunning()) {
			Thread.sleep(1000);
		}

		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());
	}

	public void testReturnFalseStep() throws Exception {

		WdlApp app = WdlReader.loadAppFromFile("test-data/return-false.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (job.isRunning()) {
			Thread.sleep(1000);
		}

		assertEquals(AbstractJob.STATE_FAILED, job.getState());
	}

	public void testReturnExceptionStep() throws Exception {

		WdlApp app = WdlReader
				.loadAppFromFile("test-data/return-exception.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (job.isRunning()) {
			Thread.sleep(1000);
		}

		assertEquals(AbstractJob.STATE_FAILED, job.getState());
	}

	public void testReturnTrueInSetupStep() throws Exception {

		WdlApp app = WdlReader
				.loadAppFromFile("test-data/return-true-in-setup.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (job.isRunning()) {
			Thread.sleep(1000);
		}

		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());
	}

	public void testReturnFalseInSetupStep() throws Exception {

		WdlApp app = WdlReader
				.loadAppFromFile("test-data/return-false-in-setup.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (job.isRunning()) {
			Thread.sleep(1000);
		}

		assertEquals(AbstractJob.STATE_FAILED, job.getState());
	}

	public void testWriteTextToFileJob() throws Exception {

		WdlApp app = WdlReader
				.loadAppFromFile("test-data/write-text-to-file.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("inputtext", "lukas_text");

		CloudgeneJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (job.isRunning()) {
			Thread.sleep(1000);
		}

		Settings settings = TestEnvironment.getInstance().getSettings();
		String path = job.getOutputParams().get(0).getFiles().get(0).getPath();
		String filename = FileUtil.path(settings.getLocalWorkspace(), path);
		String content = FileUtil.readFileAsString(filename);

		assertEquals("lukas_text", content);
		assertEquals(job.getState(), AbstractJob.STATE_SUCCESS);
	}

	public void testWriteTextToFileOnFailureInStepJob() throws Exception {

		WdlApp app = WdlReader
				.loadAppFromFile("test-data/write-text-to-file-on-failure.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("inputtext", "lukas_text");

		CloudgeneJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (job.isRunning()) {
			Thread.sleep(1000);
		}
		Thread.sleep(4000);
		Settings settings = TestEnvironment.getInstance().getSettings();
		String path = job.getOutputParams().get(0).getFiles().get(0).getPath();
		String filename = FileUtil.path(settings.getLocalWorkspace(), path);
		String content = FileUtil.readFileAsString(filename);

		assertEquals("lukas_text", content);
		assertEquals(job.getState(), AbstractJob.STATE_FAILED);
	}

	public void testWriteTextToFileOnFailureInSetupJob() throws Exception {

		WdlApp app = WdlReader
				.loadAppFromFile("test-data/write-text-to-file-on-failure2.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("inputtext", "lukas_text");

		CloudgeneJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (job.isRunning()) {
			Thread.sleep(1000);
		}

		Thread.sleep(4000);

		Settings settings = TestEnvironment.getInstance().getSettings();
		String path = job.getOutputParams().get(0).getFiles().get(0).getPath();
		String filename = FileUtil.path(settings.getLocalWorkspace(), path);
		String content = FileUtil.readFileAsString(filename);

		assertEquals("lukas_text", content);
		assertEquals(job.getState(), AbstractJob.STATE_FAILED);
	}

	/*
	 * public void testJobWithWrongParams() throws Exception {
	 * 
	 * WdlApp app = WdlReader.loadAppFromFile("test-data/return-true.yaml");
	 * 
	 * Map<String, String> params = new HashMap<String, String>();
	 * params.put("wrong-param-name", "input-file");
	 * 
	 * CloudgeneJob job = createJobFromWdl(app, params); engine.submit(job);
	 * while (job.isRunning()) { Thread.sleep(1000); }
	 * assertEquals(job.getState(), AbstractJob.STATE_FAILED); }
	 */

	public CloudgeneJob createJobFromWdl(WdlApp app, Map<String, String> inputs)
			throws Exception {

		User user = TestEnvironment.getInstance().getUser();
		Settings settings = TestEnvironment.getInstance().getSettings();

		String id = "test_" + System.currentTimeMillis();

		String hdfsWorkspace = HdfsUtil.path(settings.getHdfsWorkspace(), id);
		String localWorkspace = FileUtil.path(settings.getLocalWorkspace(), id);
		FileUtil.createDirectory(localWorkspace);

		CloudgeneJob job = new CloudgeneJob(user, id, app.getMapred(), inputs);
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
