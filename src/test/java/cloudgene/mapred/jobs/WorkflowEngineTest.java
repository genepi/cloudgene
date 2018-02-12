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
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() > 0);
		assertTrue(job.getEndTime() > 0);
		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());
	}

	public void testReturnFalseStep() throws Exception {

		WdlApp app = WdlReader.loadAppFromFile("test-data/return-false.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() > 0);
		assertTrue(job.getEndTime() > 0);
		assertEquals(AbstractJob.STATE_FAILED, job.getState());
	}

	public void testReturnExceptionStep() throws Exception {

		WdlApp app = WdlReader.loadAppFromFile("test-data/return-exception.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() > 0);
		assertTrue(job.getEndTime() > 0);
		assertEquals(AbstractJob.STATE_FAILED, job.getState());
	}

	public void testReturnTrueInSetupStep() throws Exception {

		WdlApp app = WdlReader.loadAppFromFile("test-data/return-true-in-setup.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);

		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		// no steps
		assertTrue(job.getStartTime() == 0);
		assertTrue(job.getEndTime() == 0);
		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());
	}

	public void testReturnFalseInSetupStep() throws Exception {

		WdlApp app = WdlReader.loadAppFromFile("test-data/return-false-in-setup.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		// no steps
		assertTrue(job.getStartTime() == 0);
		assertTrue(job.getEndTime() == 0);
		assertEquals(AbstractJob.STATE_FAILED, job.getState());
	}

	public void testReturnTrueInSecondSetupStep() throws Exception {

		WdlApp app = WdlReader.loadAppFromFile("test-data/return-true-in-setup2.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);

		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		// no steps
		assertTrue(job.getStartTime() == 0);
		assertTrue(job.getEndTime() == 0);
		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());
	}

	public void testReturnTrueInSecondSetupStepAndNormalStep() throws Exception {

		WdlApp app = WdlReader.loadAppFromFile("test-data/return-true-in-setup3.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);

		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		// one steps
		assertTrue(job.getStartTime() > 0);
		assertTrue(job.getEndTime() > 0);
		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());
	}

	public void testHiddenInputsAndDefaultValues() throws Exception {

		WdlApp app = WdlReader.loadAppFromFile("test-data/print-hidden-inputs.yaml");

		Map<String, String> inputs = new HashMap<String, String>();

		AbstractJob job = createJobFromWdl(app, inputs);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() > 0);
		assertTrue(job.getEndTime() > 0);
		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());
		
		//check step ouputs
		assertEquals("text1: my-value\n",  job.getSteps().get(0).getLogMessages().get(0).getMessage());
		assertEquals("checkbox1: true\n",  job.getSteps().get(1).getLogMessages().get(0).getMessage());
		assertEquals("list1: value1\n",  job.getSteps().get(2).getLogMessages().get(0).getMessage());
		assertEquals("text2: my-value\n",  job.getSteps().get(3).getLogMessages().get(0).getMessage());
		assertEquals("checkbox2: true\n",  job.getSteps().get(4).getLogMessages().get(0).getMessage());
		assertEquals("list2: value1\n",  job.getSteps().get(5).getLogMessages().get(0).getMessage());
		
		
	}
	
	public void testReturnWriteFileInSecondSetupStep() throws Exception {

		String myContent = "test-test-test-test-text";

		WdlApp app = WdlReader.loadAppFromFile("test-data/write-file-in-setup.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("inputtext", myContent);

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());

		Settings settings = TestServer.getInstance().getSettings();
		String path = job.getOutputParams().get(0).getFiles().get(0).getPath();
		String filename = FileUtil.path(settings.getLocalWorkspace(), path);
		String content = FileUtil.readFileAsString(filename);
		assertEquals(myContent, content);

		app = WdlReader.loadAppFromFile("test-data/write-file-in-setup-failure.yaml");

		params = new HashMap<String, String>();
		params.put("inputtext", myContent);

		job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertEquals(AbstractJob.STATE_FAILED, job.getState());

		System.out.println("ok:" + job.getOutputParams().get(0).getValue());

		path = job.getOutputParams().get(0).getFiles().get(0).getPath();
		filename = FileUtil.path(settings.getLocalWorkspace(), path);
		content = FileUtil.readFileAsString(filename);
		assertEquals(myContent, content);
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);

		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		// one steps
		assertTrue(job.getStartTime() > 0);
		assertTrue(job.getEndTime() > 0);

	}

	public void testEmptyStepList() throws Exception {

		WdlApp app = WdlReader.loadAppFromFile("test-data/no-steps.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("inputtext", "test");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() == 0);
		assertTrue(job.getEndTime() == 0);
		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());
	}

	public void testReturnFalseInSecondSetupStep() throws Exception {

		WdlApp app = WdlReader.loadAppFromFile("test-data/return-false-in-setup2.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() == 0);
		assertTrue(job.getEndTime() == 0);
		assertEquals(AbstractJob.STATE_FAILED, job.getState());
	}

	public void testWriteTextToFileJob() throws Exception {

		WdlApp app = WdlReader.loadAppFromFile("test-data/write-text-to-file.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("inputtext", "lukas_text");

		CloudgeneJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}

		Settings settings = TestServer.getInstance().getSettings();
		String path = job.getOutputParams().get(0).getFiles().get(0).getPath();
		String filename = FileUtil.path(settings.getLocalWorkspace(), path);
		String content = FileUtil.readFileAsString(filename);

		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() > 0);
		assertTrue(job.getEndTime() > 0);
		assertEquals("lukas_text", content);
		assertEquals(job.getState(), AbstractJob.STATE_SUCCESS);
	}

	public void testWriteTextToFileOnFailureInStepJob() throws Exception {

		WdlApp app = WdlReader.loadAppFromFile("test-data/write-text-to-file-on-failure.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("inputtext", "lukas_text");

		CloudgeneJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		Thread.sleep(4000);
		Settings settings = TestServer.getInstance().getSettings();
		String path = job.getOutputParams().get(0).getFiles().get(0).getPath();
		String filename = FileUtil.path(settings.getLocalWorkspace(), path);
		String content = FileUtil.readFileAsString(filename);
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() > 0);
		assertTrue(job.getEndTime() > 0);
		assertEquals("lukas_text", content);
		assertEquals(job.getState(), AbstractJob.STATE_FAILED);
	}

	public void testWriteTextToFileOnFailureInSetupJob() throws Exception {

		WdlApp app = WdlReader.loadAppFromFile("test-data/write-text-to-file-on-failure2.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("inputtext", "lukas_text");

		CloudgeneJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}

		Thread.sleep(4000);

		Settings settings = TestServer.getInstance().getSettings();
		String path = job.getOutputParams().get(0).getFiles().get(0).getPath();
		String filename = FileUtil.path(settings.getLocalWorkspace(), path);
		String content = FileUtil.readFileAsString(filename);
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		// steps not executed
		assertTrue(job.getStartTime() == 0);
		assertTrue(job.getEndTime() == 0);
		assertEquals("lukas_text", content);
		assertEquals(job.getState(), AbstractJob.STATE_FAILED);
	}

	public void testWriteTextToFileOnFailureInSecondSetupJob() throws Exception {

		WdlApp app = WdlReader.loadAppFromFile("test-data/write-text-to-file-on-failure3.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("inputtext", "lukas_text");

		CloudgeneJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}

		Thread.sleep(4000);

		Settings settings = TestServer.getInstance().getSettings();
		String path = job.getOutputParams().get(0).getFiles().get(0).getPath();
		String filename = FileUtil.path(settings.getLocalWorkspace(), path);
		String content = FileUtil.readFileAsString(filename);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		// no steps executed
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getStartTime() == 0);
		assertTrue(job.getEndTime() == 0);
		assertEquals("lukas_text", content);
		assertEquals(job.getState(), AbstractJob.STATE_FAILED);
	}

	public void testThreeTasksStep() throws Exception {

		WdlApp app = WdlReader.loadAppFromFile("test-data/three-tasks.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
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
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() > 0);
		assertTrue(job.getEndTime() > 0);
	}

	public void testWriteTextToStdOutStep() throws Exception {

		WdlApp app = WdlReader.loadAppFromFile("test-data/write-text-to-std-out.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}

		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());

		String stdout = FileUtil.path(TestServer.getInstance().getSettings().getLocalWorkspace(), job.getId(),
				"std.out");
		String contentStdOut = FileUtil.readFileAsString(stdout);

		String log = FileUtil.path(TestServer.getInstance().getSettings().getLocalWorkspace(), job.getId(), "job.txt");
		String contentlog = FileUtil.readFileAsString(log);

		assertTrue(contentStdOut.contains("taks write to system out"));
		assertTrue(contentStdOut.contains("taks write to system out2"));
		assertTrue(contentStdOut.contains("taks write to system out3"));

		assertTrue(contentlog.contains("taks write to log"));
		assertTrue(contentlog.contains("taks write to log2"));
		assertTrue(contentlog.contains("taks write to log3"));
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() > 0);
		assertTrue(job.getEndTime() > 0);
	}

	public void testApplicationLinks() throws Exception {

		WdlApp app = WdlReader.loadAppFromFile("test-data/app-links.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("app", "apps@app-links-child");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() > 0);
		assertTrue(job.getEndTime() > 0);
		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());

		Message message = job.getSteps().get(0).getLogMessages().get(0);
		assertEquals(Message.OK, message.getType());
		assertTrue(message.getMessage().contains("property1:hey!"));
		assertTrue(message.getMessage().contains("property2:hey2!"));
		assertTrue(message.getMessage().contains("property3:hey3!"));

	}

	public void testApplicationLinksWrongApplication() throws Exception {

		WdlApp app = WdlReader.loadAppFromFile("test-data/app-links.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("app", "apps@app-links-child-wrong-id");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() == 0);
		assertTrue(job.getEndTime() == 0);
		assertEquals(AbstractJob.STATE_FAILED, job.getState());

	}

	public void testApplicationLinksWrongPermissions() throws Exception {

		WdlApp app = WdlReader.loadAppFromFile("test-data/app-links.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("app", "apps@app-links-child-protected");

		AbstractJob job = createJobFromWdlAsUser(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() == 0);
		assertTrue(job.getEndTime() == 0);
		assertEquals(AbstractJob.STATE_FAILED, job.getState());

	}

	public void testApplicationInstallation() throws Exception {

		WdlApp app = WdlReader.loadAppFromFile("test-data/app-installation.yaml");

		Map<String, String> params = new HashMap<String, String>();

		AbstractJob job = createJobFromWdl(app, params);
		job.forceInstallation(true);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}

		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() > 0);
		assertTrue(job.getEndTime() > 0);
		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());

		// single file
		Message message = job.getSteps().get(0).getLogMessages().get(0);
		assertEquals(Message.OK, message.getType());
		assertTrue(message.getMessage().equals("content of metafile.txt"));

		// folder file1
		message = job.getSteps().get(1).getLogMessages().get(0);
		assertEquals(Message.OK, message.getType());
		assertTrue(message.getMessage().equals("content of file1.txt"));

		// folder file2
		message = job.getSteps().get(2).getLogMessages().get(0);
		assertEquals(Message.OK, message.getType());
		assertTrue(message.getMessage().equals("content of file2.txt"));

		// zip file1
		message = job.getSteps().get(3).getLogMessages().get(0);
		assertEquals(Message.OK, message.getType());
		assertTrue(message.getMessage().equals("content of file1.txt"));

		// zip file2
		message = job.getSteps().get(4).getLogMessages().get(0);
		assertEquals(Message.OK, message.getType());
		assertTrue(message.getMessage().equals("content of file2.txt"));

		// gz file1
		message = job.getSteps().get(5).getLogMessages().get(0);
		assertEquals(Message.OK, message.getType());
		assertTrue(message.getMessage().equals("content of file1.txt"));

		// gz file2
		message = job.getSteps().get(6).getLogMessages().get(0);
		assertEquals(Message.OK, message.getType());
		assertTrue(message.getMessage().equals("content of file2.txt"));

		// http single file
		message = job.getSteps().get(7).getLogMessages().get(0);
		assertEquals(Message.OK, message.getType());
		assertTrue(message.getMessage().contains("name: hello-cloudgene"));
	}

	public void testApplicationInstallationAndLinks() throws Exception {

		WdlApp app = WdlReader.loadAppFromFile("test-data/app-installation2.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("dataset", "apps@app-installation-child");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}

		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() > 0);
		assertTrue(job.getEndTime() > 0);
		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());

		Message message = job.getSteps().get(0).getLogMessages().get(0);
		assertEquals(Message.OK, message.getType());
		assertTrue(message.getMessage().equals("content of metafile2.txt"));
	}

	// TODO: merge and zip export.

	// TODO: write to hdfs temp and local temp (temp output params)!

	// TODO: check if removehdfsworkspace works!

	// TODO: check cloudgene counters (successful and failed)

	public CloudgeneJob createJobFromWdl(WdlApp app, Map<String, String> inputs) throws Exception {

		User user = TestServer.getInstance().getAdminUser();
		return createJobFromWdl(app, inputs, user);
	}

	public CloudgeneJob createJobFromWdlAsUser(WdlApp app, Map<String, String> inputs) throws Exception {

		User user = TestServer.getInstance().getUser();
		return createJobFromWdl(app, inputs, user);
	}

	public CloudgeneJob createJobFromWdl(WdlApp app, Map<String, String> inputs, User user) throws Exception {

		Settings settings = TestServer.getInstance().getSettings();

		String id = "test_" + System.currentTimeMillis();

		String hdfsWorkspace = HdfsUtil.path(settings.getHdfsWorkspace(), id);
		String localWorkspace = FileUtil.path(settings.getLocalWorkspace(), id);
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
