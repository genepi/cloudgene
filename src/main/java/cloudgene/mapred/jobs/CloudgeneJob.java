package cloudgene.mapred.jobs;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.engine.Executor;
import cloudgene.mapred.jobs.engine.Planner;
import cloudgene.mapred.jobs.engine.graph.Graph;
import cloudgene.mapred.jobs.engine.graph.GraphNode;
import cloudgene.mapred.jobs.workspace.IExternalWorkspace;
import cloudgene.mapred.jobs.workspace.WorkspaceWrapper;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlParameterInput;
import cloudgene.mapred.wdl.WdlParameterOutput;
import cloudgene.mapred.wdl.WdlParameterOutputType;
import cloudgene.mapred.wdl.WdlStep;
import genepi.io.FileUtil;

public class CloudgeneJob extends AbstractJob {

	private WdlApp app;

	private String workingDirectory;

	private Executor executor;

	public static final int MAX_DOWNLOAD = 10;

	private static Logger log = LoggerFactory.getLogger(CloudgeneJob.class);

	public CloudgeneJob() {
		super();
	}

	public void loadConfig(WdlApp app) {

		this.app = app;
		workingDirectory = app.getPath();

		// set parameter properties that are not stored in database.
		// needed for restart
		for (CloudgeneParameterOutput outputParam : outputParams) {

			for (WdlParameterOutput output : app.getWorkflow().getOutputs()) {

				if (outputParam.getName().equals(output.getId())) {
					outputParam.setMakeAbsolute(output.isMakeAbsolute());
					outputParam.setAutoExport(output.isAutoExport());
					outputParam.setZip(output.isZip());
					outputParam.setMergeOutput(output.isMergeOutput());
					outputParam.setRemoveHeader(output.isRemoveHeader());
				}

			}

		}

	}

	public CloudgeneJob(User user, String id, WdlApp app, Map<String, String> params) {
		setComplete(false);
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

	}

	@Override
	public boolean setup() {

		context = new CloudgeneContext(this);

		FileUtil.deleteDirectory(context.getLocalTemp());

		// create output directories
		FileUtil.createDirectory(context.getLocalTemp());

		try {
			context.log("Setup External Workspace on " + externalWorkspace.getName());
			externalWorkspace.setup(this.getId());
			context.setExternalWorkspace(new WorkspaceWrapper(externalWorkspace));
		} catch (Exception e) {
			writeLog(e.toString());
			log.info("Error setup external workspace", e);
			setError(e.toString());
			return false;
		}

		// create output directories
		for (CloudgeneParameterOutput param : outputParams) {

			switch (param.getType()) {
			case HDFS_FILE:
			case HDFS_FOLDER:

				throw new RuntimeException("HDFS support was removed in Cloudgene 3");

			case LOCAL_FILE:
				String filename = externalWorkspace.createFile(param.getName(), param.getName());
				param.setValue(filename);
				break;

			case LOCAL_FOLDER:
				String folder = externalWorkspace.createFolder(param.getName());
				param.setValue(folder);
				break;
			}

		}

		return true;

	}

	@Override
	public boolean hasSteps() {
		try {
			// evaluate WDL derictives
			Planner planner = new Planner();
			WdlApp app = planner.evaluateWDL(this.app, context, getSettings());
			return app.getWorkflow().getSteps().size() > 0;
		} catch (Exception e) {
			// e.printStackTrace();
			writeOutput(e.getMessage());
			setError(e.getMessage());
			return false;
		}
	}

	@Override
	public boolean execute() {

		try {

			log.info("Job " + getId() + " submitted...");

			// evaluate WDL derictives
			Planner planner = new Planner();
			WdlApp app = planner.evaluateWDL(this.app, context, getSettings());

			// create dag from wdl document
			Graph graph = planner.buildDAG(app.getWorkflow().getSteps(), app.getWorkflow(), context);

			// execute optimzed dag
			executor = new Executor();
			boolean successful = executor.execute(graph);

			if (!successful) {
				setError("Job Execution failed.");
				GraphNode failedNode = executor.getCurrentNode();
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
	public boolean executeSetupSteps() {

		try {
			// evaluate WDL
			Planner planner = new Planner();
			WdlApp app = planner.evaluateWDL(this.app, context, getSettings());

			// if a single setup step is set, add it to setups
			WdlStep setup = app.getWorkflow().getSetup();
			if (setup != null) {
				app.getWorkflow().getSetups().add(0, setup);
			}

			Graph graph = planner.buildDAG(app.getWorkflow().getSetups(), app.getWorkflow(), context);

			// execute optimized DAG
			executor = new Executor();
			boolean successful = executor.execute(graph);

			if (!successful) {
				setError("Job Execution failed.");
				GraphNode failedNode = executor.getCurrentNode();
				executeFailureStep(failedNode.getStep());
				return false;
			}

			setError(null);
			return true;
		} catch (Exception e) {
			// e.printStackTrace();
			writeOutput(e.getMessage());
			setError(e.getMessage());
			return false;
		}

	}

	@Override
	public boolean before() {

		return true;

	}

	@Override
	public boolean onFailure() {

		cleanUp();

		return true;
	}

	public boolean executeFailureStep(WdlStep failedStep) {

		WdlStep step = app.getWorkflow().getOnFailure();

		if (step != null) {
			try {
				writeLog("Executing onFailure... ");
				GraphNode node = new GraphNode(step, context);
				context.setData("cloudgene.failedStep", failedStep);
				context.setData("cloudgene.failedStep.classname", failedStep.getClassname());
				node.run();
				boolean result = node.isSuccessful();
				if (result) {

					// export parameters generated by onFailure step
					for (CloudgeneParameterOutput out : getOutputParams()) {
						if (out.isAutoExport() && step.getGenerates().contains(out.getName())) {
							log.info("Export parameter '" + out.getName() + "'...");
							context.println("Export parameter '" + out.getName() + "'...");
							exportParameter(out);
						}
					}

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

		cleanUp();

		return true;
	}

	@Override
	public boolean cleanUp() {

		try {
			externalWorkspace.cleanup(getId());
		} catch (IOException e) {
			writeLog("Cleanup failed.");
			writeLog(e.getMessage());
			setError(e.getMessage());
			return false;
		}

		return true;
	}

	@Override
	public boolean after() {

		// create output zip file for hdfs folders
		for (CloudgeneParameterOutput out : getOutputParams()) {

			if (out.isDownload() && !out.isAutoExport()) {
				// export to local folder for faster download
				exportParameter(out);

			}

		}

		return true;
	}

	public boolean exportParameter(CloudgeneParameterOutput out) {

		writeLog("  Exporting parameter " + out.getName() + "...");

		if (out.getType() == WdlParameterOutputType.HDFS_FOLDER || out.getType() == WdlParameterOutputType.HDFS_FILE) {

			throw new RuntimeException("HDFS support was removed in Cloudgene 3");

		}

		out.setJobId(getId());

		List<Download> downloads = externalWorkspace.getDownloads(out.getValue());
		for (Download download : downloads) {
			download.setParameter(out);
			download.setCount(MAX_DOWNLOAD);
		}
		writeLog("  Added " + downloads.size() + " downloads.");

		List<Download> customDownloads = context.getDownloads(out.getName());
		if (customDownloads != null) {
			for (Download download : customDownloads) {
				download.setParameter(out);
				download.setCount(MAX_DOWNLOAD);
			}
			writeLog("  Added " + customDownloads.size() + " custom downloads.");
			downloads.addAll(customDownloads);
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
			setProgress(executor.getProgress());

		} else {
			setProgress(-1);
		}

	}

	public IExternalWorkspace getExternalWorkspace() {
		return externalWorkspace;
	}

}
