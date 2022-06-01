package cloudgene.mapred.jobs;

import cloudgene.mapred.apps.Application;
import cloudgene.mapred.apps.ApplicationInstaller;
import cloudgene.mapred.apps.ApplicationRepository;
import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.engine.Executor;
import cloudgene.mapred.jobs.engine.Planner;
import cloudgene.mapred.jobs.engine.graph.Graph;
import cloudgene.mapred.jobs.engine.graph.GraphNode;
import cloudgene.mapred.jobs.workspace.ExternalWorkspaceFactory;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.*;
import cloudgene.sdk.internal.IExternalWorkspace;
import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class CloudgeneJob extends AbstractJob {

	private WdlApp app;

	private String workingDirectory;

	private Executor executor;

	public static final int MAX_DOWNLOAD = 10;

	private static final Log log = LogFactory.getLog(CloudgeneJob.class);

	private String externalOutput = null;

	private IExternalWorkspace externalWorkspace;

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

		externalOutput = params.get("external-output");

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
		// context.updateInputParameters();
		context.setupOutputParameters(app.getWorkflow().hasHdfsOutputs());

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
	public boolean executeInstallation(boolean forceInstallation) {

		try {

			Settings settings = getSettings();
			ApplicationRepository repository = settings.getApplicationRepository();

			// find dependencies
			List<WdlApp> applications = new Vector<>();

			// install application
			applications.add(app);

			for (CloudgeneParameterInput input : getInputParams()) {
				if (input.getType() == WdlParameterInputType.APP_LIST) {
					String value = input.getValue();
					String linkedAppId = value;
					if (value.startsWith("apps@")) {
						linkedAppId = value.replaceAll("apps@", "");
					}

					if (!value.isEmpty()) {
						Application linkedApp = repository.getByIdAndUser(linkedAppId, getUser());
						if (linkedApp != null) {
							applications.add(linkedApp.getWdlApp());
							// update environment variables
							Map<String, String> envApp = Environment.getApplicationVariables(linkedApp.getWdlApp(),
									settings);
							Map<String, String> envJob = Environment.getJobVariables(context);
							Map<String, Object> properties = linkedApp.getWdlApp().getProperties();
							for (String property : properties.keySet()) {
								Object propertyValue = properties.get(property);
								if (propertyValue instanceof String) {
									propertyValue = Environment.env(propertyValue.toString(), envApp);
									propertyValue = Environment.env(propertyValue.toString(), envJob);
								}
								properties.put(property, propertyValue);
							}

							getContext().setData(input.getName(), properties);
						} else {
							String error = "Application " + linkedAppId + " is not installed or wrong permissions.";
							log.info(error);
							writeOutput(error);
							setError(error);
							return false;
						}
					}
				}
			}

			for (WdlApp app : applications) {

				log.info("Job " + getId() + ": executing installation for " + app.getId() + "...");

				if (app.needsInstallation()) {

					writeLog("  Preparing Application '" + app.getName() + "'...");
					boolean installed = ApplicationInstaller.isInstalled(app, settings);
					if (!installed || forceInstallation) {
						try {
							writeLog("  Installing Application " + app.getId() + "...");
							ApplicationInstaller.install(app, settings);
							log.info("Installation of application " + app.getId() + " finished.");
							writeLog("  Installation finished.");
						} catch (IOException e) {
							log.info("Installation of application " + app.getId() + " failed.", e);
							writeLog(" Installation failed.");
							writeLog(e.getMessage());
							setError(e.getMessage());
							return false;
						}
					} else {
						String info = "Application '" + app.getName() + "'is already installed.";
						log.info(info);
						writeLog("  " + info);
					}
				}

			}

			// if default location is set, override user defined
			if (!settings.getExternalWorkspaceLocation().isEmpty()) {
				externalOutput = settings.getExternalWorkspaceLocation();
			}

			externalWorkspace = ExternalWorkspaceFactory.get(settings.getExternalWorkspaceType(), externalOutput);

			if (externalWorkspace != null) {

				try {
					context.log("Setup External Workspace on " + externalWorkspace.getName());
					externalWorkspace.setup(this.getId());
					context.setExternalWorkspace(externalWorkspace);
				} catch (Exception e) {
					writeLog(e.toString());
					log.info("Error setup external workspace", e);
					setError(e.toString());
					return false;
				}

			}

			return true;
		} catch (Exception e) {
			writeLog("Installation of Application " + getApplicationId() + " failed.");
			log.info("Installation of application " + app.getId() + " failed.", e);
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

		// delete local temp folders

		writeLog("Cleaning up uploaded local files...");
		FileUtil.deleteDirectory(context.getLocalInput());

		writeLog("Cleaning up temporary local files...");
		FileUtil.deleteDirectory(context.getLocalTemp());

		if (app.getWorkflow().hasHdfsOutputs()) {

			try {
				// delete hdfs temp folders
				writeLog("Cleaning up temporary hdfs files...");
				HdfsUtil.delete(context.getHdfsTemp());

				// delete hdfs workspace
				if (isRemoveHdfsWorkspace()) {
					if (context.getHdfsOutput() != null) {
						writeLog("Cleaning up hdfs files...");
						HdfsUtil.delete(context.getHdfsOutput());
						HdfsUtil.delete(context.getHdfsInput());
					}
				}
			} catch (Exception e) {
				log.warn("Warning: problems during hdfs cleanup.");
			} catch (Error e) {
				log.warn("Warning: problems during hdfs cleanup.");
			}
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

		String localOutput = context.getLocalOutput();
		String workspace = getHdfsWorkspace();

		if (out.getType() == WdlParameterOutputType.HDFS_FOLDER) {

			String localOutputDirectory = FileUtil.path(localOutput, out.getName());

			FileUtil.createDirectory(localOutputDirectory);

			String filename = context.getOutput(out.getName());
			String hdfsPath = null;
			if (filename.startsWith("hdfs://") || filename.startsWith("file:/")) {
				hdfsPath = filename;

			} else {

				hdfsPath = HdfsUtil.makeAbsolute(HdfsUtil.path(workspace, filename));
			}

			if (HdfsUtil.exists(hdfsPath)) {

				if (out.isZip()) {

					String zipName = FileUtil.path(localOutputDirectory, out.getName() + ".zip");

					if (out.isMergeOutput()) {

						HdfsUtil.compressAndMerge(zipName, hdfsPath, out.isRemoveHeader());

					} else {

						HdfsUtil.compress(zipName, hdfsPath);

					}

				} else {

					if (out.isMergeOutput()) {

						HdfsUtil.exportDirectoryAndMerge(localOutputDirectory, out.getName(), hdfsPath,
								out.isRemoveHeader());

					} else {

						HdfsUtil.exportDirectory(localOutputDirectory, out.getName(), hdfsPath);

					}

				}

			}

		}

		if (out.getType() == WdlParameterOutputType.HDFS_FILE) {

			String localOutputDirectory = FileUtil.path(localOutput, out.getName());

			FileUtil.createDirectory(localOutputDirectory);

			String filename = context.getOutput(out.getName());
			String hdfsPath = null;
			if (filename.startsWith("hdfs://") || filename.startsWith("file:/")) {

				hdfsPath = filename;

			} else {

				hdfsPath = HdfsUtil.makeAbsolute(HdfsUtil.path(workspace, filename));
			}

			if (!out.isZip()) {

				if (HdfsUtil.exists(hdfsPath)) {
					HdfsUtil.exportFile(localOutputDirectory, hdfsPath);
				}

			} else {
				if (HdfsUtil.exists(hdfsPath)) {
					HdfsUtil.compressFile(localOutputDirectory, hdfsPath);
				}
			}

		}

		out.setJobId(getId());

		String name = out.getName();
		String n = FileUtil.path(localOutput, name);

		File f = new File(n);

		List<Download> downloads = new Vector<Download>();

		if (f.exists() && f.isDirectory()) {

			try {
				exportFolder(out, "", name, f, downloads);
			} catch (Exception e) {

				writeLog("Export paramater '" + out.getName() + "' failed:" + e);
				log.info("Export paramater '" + out.getName() + "' failed:" + e);
				setError("Export paramater '" + out.getName() + "' failed:" + e);
				return false;
			}
			writeLog("  Added " + downloads.size() + " downloads.");
		}

		List<Download> customDownloads = context.getDownloads(out.getName());
		if (customDownloads != null) {
			for (Download download : customDownloads) {
				download.setParameter(out);
			}
			writeLog("  Added " + customDownloads.size() + " custom downloads.");
			downloads.addAll(customDownloads);
		}

		Collections.sort(downloads);
		out.setFiles(downloads);

		return true;
	}

	private void exportFolder(CloudgeneParameterOutput out, String prefix, String name, File folder,
			List<Download> downloads) throws Exception {
		if (folder.exists() && folder.isDirectory()) {

			File[] files = folder.listFiles();

			for (int i = 0; i < files.length; i++) {
				if (!files[i].isDirectory()) {

					String filename = prefix.equals("") ? files[i].getName() : prefix + "/" + files[i].getName();
					String id = name + "/" + files[i].getName();
					String size = FileUtils.byteCountToDisplaySize(files[i].length());
					String hash = HashUtil.getSha256(filename + id + size + getId() + (Math.random() * 100000));
					Download download = new Download();
					download.setName(filename);
					download.setPath(FileUtil.path(getId(), id));
					download.setSize(size);
					download.setHash(hash);
					download.setParameter(out);
					download.setCount(MAX_DOWNLOAD);
					downloads.add(download);

					if (externalWorkspace != null) {
						// upload to s3 bucket, update path and delete local file
						try {
							context.log("  Uploading file " + files[i].getAbsolutePath() + " to external workspace");
							String url = externalWorkspace.upload(name, files[i]);
							context.log("  Uploaded file to " + url + ".");
							download.setPath(url);
							files[i].delete();
						} catch (Exception e) {
							throw new Exception("Error uploading output '" + files[i].getAbsolutePath() + "'. " + e);
						}

					}

				} else {
					exportFolder(out, prefix.equals("") ? files[i].getName() : prefix + "/" + files[i].getName(),
							name + "/" + files[i].getName(), files[i], downloads);
				}
			}
		}
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

}
