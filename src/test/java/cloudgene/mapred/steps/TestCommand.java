package cloudgene.mapred.steps;

import genepi.hadoop.HdfsUtil;
import genepi.hadoop.common.WorkflowContext;
import genepi.io.FileUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.jobs.Message;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.util.junit.TestServer;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlReader;

public class TestCommand extends TestCase {

	private WorkflowEngine engine;

	@Override
	protected void setUp() throws Exception {
		engine = TestServer.getInstance().startWorkflowEngineWithoutServer();

	}

	public void testValidCommand() throws Exception {
		WdlApp app = WdlReader.loadAppFromFile("test-data/command/valid-command.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(1000);
		}

		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());

		List<Message> messages = job.getSteps().get(0).getLogMessages();
		assertEquals(1, messages.size());
		assertEquals(messages.get(0).getType(), WorkflowContext.OK);
		assertTrue(messages.get(0).getMessage().contains("Execution successful."));

		String stdout = FileUtil.path(TestServer.getInstance().getSettings().getLocalWorkspace(), job.getId(),
				"std.out");
		String contentStdOut = FileUtil.readFileAsString(stdout);

		// simple ls result check
		assertTrue(contentStdOut.contains("invalid-command.yaml"));

		String jobLog = FileUtil.path(TestServer.getInstance().getSettings().getLocalWorkspace(), job.getId(),
				"job.txt");
		String contentjobLog = FileUtil.readFileAsString(jobLog);

		// simple check if exit code = 0
		assertTrue(contentjobLog.contains("Exit Code: 0"));

	}

	public void testInvalidCommand() throws Exception {
		WdlApp app = WdlReader.loadAppFromFile("test-data/command/invalid-command.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(1000);
		}

		assertEquals(AbstractJob.STATE_FAILED, job.getState());

		List<Message> messages = job.getSteps().get(0).getLogMessages();
		assertEquals(1, messages.size());
		assertEquals(messages.get(0).getType(), WorkflowContext.ERROR);
		assertTrue(messages.get(0).getMessage().contains("Command '/bin/lukas/forer' was not found."));
	}

	/*
	 * public void testInvalidParameters() throws Exception{ WdlApp app =
	 * WdlReader.loadAppFromFile("test-data/command/invalid-parameters.yaml");
	 * 
	 * Map<String, String> params = new HashMap<String, String>();
	 * params.put("input", "input-file");
	 * 
	 * AbstractJob job = createJobFromWdl(app, params); engine.submit(job);
	 * while (job.isRunning()) { Thread.sleep(1000); }
	 * 
	 * assertEquals(AbstractJob.STATE_FAILED, job.getState());
	 * 
	 * List<Message> messages = job.getSteps().get(0).getLogMessages();
	 * assertEquals(1, messages.size()); assertEquals(messages.get(0).getType(),
	 * WorkflowContext.ERROR);
	 * assertTrue(messages.get(0).getMessage().contains("Execution failed."));
	 * 
	 * String stdout = FileUtil.path(TestServer.getInstance().getSettings()
	 * .getLocalWorkspace(), job.getId(), "std.out");
	 * System.out.println(stdout); String contentStdOut =
	 * FileUtil.readFileAsString(stdout);
	 * 
	 * //simple check for unrecognized option
	 * assertTrue(contentStdOut.contains("unrecognized option"));
	 * 
	 * //simple check if exit code = 1
	 * assertFalse(contentStdOut.contains("Exit Code: 0"));
	 * 
	 * }
	 */

	// TODO: check file staging

	public CloudgeneJob createJobFromWdl(WdlApp app, Map<String, String> inputs) throws Exception {

		User user = TestServer.getInstance().getUser();
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
