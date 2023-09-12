package cloudgene.mapred.jobs;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.engine.ExecutableStep;
import cloudgene.mapred.jobs.engine.Executor;
import cloudgene.mapred.jobs.engine.Planner;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlParameterInput;
import cloudgene.mapred.wdl.WdlParameterOutput;
import cloudgene.mapred.wdl.WdlParameterOutputType;
import cloudgene.mapred.wdl.WdlStep;

public class CloudgeneJob extends AbstractJob {

	private static final String CLOUDGENE_LOGS_PARAM = "cloudgene_logs";

	private WdlApp app;

	private String workingDirectory;

	private Executor executor;

	public static final int MAX_DOWNLOAD = 10;

	private static Logger log = LoggerFactory.getLogger(CloudgeneJob.class);

	public CloudgeneJob() {
		super();
	}

	public CloudgeneJob(User user, String id, WdlApp app, Map<String, String> params) {
		this.app = app;
		setId(id);
		setUser(user);
		workingDirectory = app.getPath();

		// init parameters
		inputParams = new Vector<CloudgeneParameterInput>();
		for (WdlParameterInput input : app.getWorkflow().getInputs()) {
			CloudgeneParameterInput newInput = new CloudgeneParameterInput(input);
			newInput.setJob(this);

			if (params.containsKey(input.getId())) {
				newInput.setValue(params.get(input.getId()));
			}

			inputParams.add(newInput);
		}

		outputParams = new Vector<CloudgeneParameterOutput>();
		for (WdlParameterOutput output : app.getWorkflow().getOutputs()) {
			CloudgeneParameterOutput newOutput = new CloudgeneParameterOutput(output);
			newOutput.setJob(this);
			outputParams.add(newOutput);
		}

		initLogOutput();

	}

	public void loadApp(WdlApp app) {

		this.app = app;
		workingDirectory = app.getPath();

		// find logOutput and remove from outputs
		for (CloudgeneParameterOutput param : getOutputParams()) {
			if (!param.getName().equals(CLOUDGENE_LOGS_PARAM)) {
				continue;
			}
			logOutput = param;
		}
		if (logOutput != null) {
			getOutputParams().remove(logOutput);
		}

	}

	protected void initLogOutput() {
		logOutput = new CloudgeneParameterOutput();
		logOutput.setAdminOnly(true);
		logOutput.setDownload(true);
		logOutput.setDescription("Logs");
		logOutput.setName(CLOUDGENE_LOGS_PARAM);
		logOutput.setType(WdlParameterOutputType.LOCAL_FOLDER);
		logOutput.setJob(this);
	}

	@Override
	public boolean setup() {

		context = new CloudgeneContext(this);

		try {
			log.info("[Job {}] Setup workspace {}'", getId(), workspace.getName());
			context.log("Setup External Workspace on " + workspace.getName());
			workspace.setup(this.getId());
			context.setWorkspace(workspace);
		} catch (Exception e) {
			writeLog(e.toString());
			log.error("[Job {}] Error setup external workspace failed.", getId(), e);
			setError(e.toString());
			return false;
		}

		// create output directories
		for (CloudgeneParameterOutput param : outputParams) {

			switch (param.getType()) {
			case HDFS_FILE:
			case HDFS_FOLDER:
				log.info("[Job {}] Setting output param '{}' failed. HDFS support was removed in Cloudgene 3'", getId(),
						param.getName());
				throw new RuntimeException("HDFS support was removed in Cloudgene 3");

			case LOCAL_FILE:
				String filename = workspace.createFile(param.getName(), param.getName());
				param.setValue(filename);
				log.info("[Job {}] Set output file '{}' to '{}'", getId(), param.getName(), param.getValue());
				break;

			case LOCAL_FOLDER:
				String folder = workspace.createFolder(param.getName());
				param.setValue(folder);
				log.info("[Job {}] Set output folder '{}' to '{}'", getId(), param.getName(), param.getValue());
				break;
			}

		}

		return true;

	}

	@Override
	public boolean execute() {

		try {

			// evaluate WDL and replace all variables (e.g. ${job_id})
			Planner planner = new Planner();
			WdlApp app = planner.evaluateWDL(this.app, context, getSettings());

			// merge setup steps and normal steps
			List<WdlStep> steps = new Vector<WdlStep>(app.getWorkflow().getSetups());
			steps.addAll(app.getWorkflow().getSteps());
			log.info("[Job {}] execute {} steps", getId(), steps.size());

			// execute steps
			executor = new Executor();
			boolean successful = executor.execute(steps, context);

			if (!successful) {
				setError("Job Execution failed.");
				ExecutableStep failedNode = executor.getCurrentNode();
				executeFailureStep(failedNode.getStep());
				return false;
			}

			setError(null);
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			writeOutput(e.getMessage());
			setError(e.getMessage());
			return false;
		}

	}

	@Override
	public boolean onFailure() {

		after();

		cleanUp();

		return true;
	}

	public boolean executeFailureStep(WdlStep failedStep) {

		WdlStep step = app.getWorkflow().getOnFailure();
		cleanUp();

		if (step == null) {
			return true;
		}

		try {
			writeLog("Executing onFailure... ");
			ExecutableStep node = new ExecutableStep(step, context);
			context.setData("cloudgene.failedStep", failedStep);
			context.setData("cloudgene.failedStep.classname", failedStep.getClassname());
			node.run();
			boolean result = node.isSuccessful();
			if (result) {
				writeLog("onFailure execution successful.");
				return true;
			} else {
				writeLog("onFailure execution failed.");
				return false;
			}

		} catch (Exception e) {
			writeLog("onFailure execution failed.");
			writeLog(e.getMessage());
			setError(e.getMessage());
			return false;
		}

	}

	@Override
	public boolean cleanUp() {

		log.info("[Job {}] Cleaning up...", getId());

		try {
			workspace.cleanup(getId());
		} catch (IOException e) {
			writeLog("Cleanup failed.");
			writeLog(e.getMessage());
			setError(e.getMessage());
			log.error("[Job {}] Clean up failed.", getId(), e);
			return false;
		}

		return true;
	}

	@Override
	public boolean after() {

		log.info("[Job {}] Export parameters...", getId());

		for (CloudgeneParameterOutput out : getOutputParams()) {
			if (out.isDownload()) {
				exportParameter(out);
			}
		}

		log.info("[Job {}] Export logs...", getId());
		List<Download> logs = workspace.getLogs();
		for (Download log : logs) {
			log.setCount(-1);
		}
		logOutput.setFiles(logs);

		return true;
	}

	public boolean exportParameter(CloudgeneParameterOutput out) {

		writeLog("  Exporting parameter " + out.getName() + "...");

		if (out.getType() == WdlParameterOutputType.HDFS_FOLDER || out.getType() == WdlParameterOutputType.HDFS_FILE) {

			throw new RuntimeException("HDFS support was removed in Cloudgene 3");

		}

		out.setJobId(getId());

		List<Download> downloads = workspace.getDownloads(out.getValue());
		for (Download download : downloads) {
			download.setParameter(out);
			download.setCount(MAX_DOWNLOAD);
			if (!out.getFiles().contains(download)) {
				out.getFiles().add(download);
				writeLog("  Added new download " + download.getName() + ".");
			} else {
				writeLog("  Download " + download.getName() + " already added.");
			}
		}
		Collections.sort(downloads);
		out.setFiles(downloads);

		return true;
	}

	public String getWorkingDirectory() {
		return workingDirectory;
	}

	public WdlApp getApp() {
		return app;
	}

	@Override
	public void kill() {
		if (executor != null) {
			executor.kill();
		}
	}

	public void updateProgress() {

		if (executor != null) {
			executor.updateProgress();
		}

	}

}
