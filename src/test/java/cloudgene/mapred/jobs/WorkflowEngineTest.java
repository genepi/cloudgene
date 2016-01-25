package cloudgene.mapred.jobs;

import genepi.io.FileUtil;

import java.util.HashMap;
import java.util.Map;

import cloudgene.mapred.jobs.util.CloudgeneTestCase;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlReader;

public class WorkflowEngineTest extends CloudgeneTestCase {

	public void testReturnTrueStep() throws Exception {

		WorkflowEngine engine = startWorkflowEngine();
		Thread engineThread = new Thread(engine);
		engineThread.start();

		WdlApp app = WdlReader.loadAppFromFile("test-data/return-true.yaml");

		Map<String, String> inputs = new HashMap<String, String>();
		inputs.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, inputs);
		engine.submit(job);
		while (job.isRunning()) {
			Thread.sleep(1000);
		}
		engineThread.stop();

		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());
	}

	public void testReturnFalseStep() throws Exception {

		WorkflowEngine engine = startWorkflowEngine();
		Thread engineThread = new Thread(engine);
		engineThread.start();

		WdlApp app = WdlReader.loadAppFromFile("test-data/return-false.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (job.isRunning()) {
			Thread.sleep(1000);
		}
		engineThread.stop();

		assertEquals(AbstractJob.STATE_FAILED, job.getState());
	}

	public void testReturnExceptionStep() throws Exception {

		WorkflowEngine engine = startWorkflowEngine();
		Thread engineThread = new Thread(engine);
		engineThread.start();

		WdlApp app = WdlReader
				.loadAppFromFile("test-data/return-exception.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (job.isRunning()) {
			Thread.sleep(1000);
		}
		engineThread.stop();

		assertEquals(AbstractJob.STATE_FAILED, job.getState());
	}

	public void testWriteTextToFileJob() throws Exception {

		WorkflowEngine engine = startWorkflowEngine();
		Thread engineThread = new Thread(engine);
		engineThread.start();

		WdlApp app = WdlReader
				.loadAppFromFile("test-data/write-text-to-file.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("inputtext", "lukas_text");

		CloudgeneJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (job.isRunning()) {
			Thread.sleep(1000);
		}
		engineThread.stop();

		String outputFilename = job.getContext().getOutput("output");
		String content = FileUtil.readFileAsString(outputFilename);

		System.out.println(FileUtil.readFileAsString(job.getStdOutFile()));

		assertEquals("lukas_text", content);
		assertEquals(job.getState(), AbstractJob.STATE_SUCCESS);
	}

	public void testJobWithWrongParams() throws Exception {

		WorkflowEngine engine = startWorkflowEngine();
		Thread engineThread = new Thread(engine);
		engineThread.start();

		WdlApp app = WdlReader.loadAppFromFile("test-data/return-true.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("wrong-param-name", "input-file");

		CloudgeneJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (job.isRunning()) {
			Thread.sleep(1000);
		}
		engineThread.stop();
		assertEquals(job.getState(), AbstractJob.STATE_FAILED);
	}

}
