package cloudgene.mapred.jobs;

import genepi.hadoop.HdfsUtil;
import genepi.hadoop.common.WorkflowContext;
import genepi.io.FileUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import cloudgene.mapred.core.User;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.util.junit.TestServer;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlReader;

public class WorkflowEngineTest extends TestCase {

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
	

	public void testReturnTrueInSecondSetupStep() throws Exception {

		WdlApp app = WdlReader
				.loadAppFromFile("test-data/return-true-in-setup2.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (job.isRunning()) {
			Thread.sleep(1000);
		}

		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());
	}

	public void testReturnFalseInSecondSetupStep() throws Exception {

		WdlApp app = WdlReader
				.loadAppFromFile("test-data/return-false-in-setup2.yaml");

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

		Settings settings = TestServer.getInstance().getSettings();
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
		Settings settings = TestServer.getInstance().getSettings();
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

		Settings settings = TestServer.getInstance().getSettings();
		String path = job.getOutputParams().get(0).getFiles().get(0).getPath();
		String filename = FileUtil.path(settings.getLocalWorkspace(), path);
		String content = FileUtil.readFileAsString(filename);

		assertEquals("lukas_text", content);
		assertEquals(job.getState(), AbstractJob.STATE_FAILED);
	}
	
	public void testWriteTextToFileOnFailureInSecondSetupJob() throws Exception {

		WdlApp app = WdlReader
				.loadAppFromFile("test-data/write-text-to-file-on-failure3.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("inputtext", "lukas_text");

		CloudgeneJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (job.isRunning()) {
			Thread.sleep(1000);
		}

		Thread.sleep(4000);

		Settings settings = TestServer.getInstance().getSettings();
		String path = job.getOutputParams().get(0).getFiles().get(0).getPath();
		String filename = FileUtil.path(settings.getLocalWorkspace(), path);
		String content = FileUtil.readFileAsString(filename);

		assertEquals("lukas_text", content);
		assertEquals(job.getState(), AbstractJob.STATE_FAILED);
	}

	public void testThreeTasksStep() throws Exception {

		WdlApp app = WdlReader.loadAppFromFile("test-data/three-tasks.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (job.isRunning()) {
			Thread.sleep(1000);
		}

		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());

		List<Message> messages = job.getSteps().get(0).getLogMessages();

		assertEquals(3, messages.size());
		assertEquals("cloudgene-task1", messages.get(0).getMessage());
		assertEquals(WorkflowContext.OK, messages.get(0).getType());
		assertEquals("cloudgene-task2", messages.get(1).getMessage());
		assertEquals(WorkflowContext.OK, messages.get(1).getType());
		assertEquals("cloudgene-task3", messages.get(2).getMessage());
		assertEquals(WorkflowContext.OK, messages.get(2).getType());

	}

	public void testWriteTextToStdOutStep() throws Exception {

		WdlApp app = WdlReader
				.loadAppFromFile("test-data/write-text-to-std-out.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (job.isRunning()) {
			Thread.sleep(1000);
		}

		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());

		String stdout = FileUtil.path(TestServer.getInstance().getSettings()
				.getLocalWorkspace(), job.getId(), "std.out");
		String contentStdOut = FileUtil.readFileAsString(stdout);

		String log = FileUtil.path(TestServer.getInstance().getSettings()
				.getLocalWorkspace(), job.getId(), "job.txt");
		String contentlog = FileUtil.readFileAsString(log);

		assertTrue(contentStdOut.contains("taks write to system out"));
		assertTrue(contentStdOut.contains("taks write to system out2"));
		assertTrue(contentStdOut.contains("taks write to system out3"));

		assertTrue(contentlog.contains("taks write to log"));
		assertTrue(contentlog.contains("taks write to log2"));
		assertTrue(contentlog.contains("taks write to log3"));

	}

	// TODO: merge and zip export.
		
	// TODO: write to hdfs temp and local temp (temp output params)!

	// TODO: check if removehdfsworkspace works!

	// TODO: check cloudgene counters (successful and failed)

	public CloudgeneJob createJobFromWdl(WdlApp app, Map<String, String> inputs)
			throws Exception {

		User user = TestServer.getInstance().getUser();
		Settings settings = TestServer.getInstance().getSettings();

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
