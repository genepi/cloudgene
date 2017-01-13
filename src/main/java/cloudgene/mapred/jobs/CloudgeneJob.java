package cloudgene.mapred.jobs;

import genepi.hadoop.HdfsUtil;
import genepi.hadoop.importer.FileItem;
import genepi.io.FileUtil;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.engine.Executor;
import cloudgene.mapred.jobs.engine.Planner;
import cloudgene.mapred.jobs.engine.graph.Graph;
import cloudgene.mapred.jobs.engine.graph.GraphNode;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlMapReduce;
import cloudgene.mapred.wdl.WdlParameter;
import cloudgene.mapred.wdl.WdlStep;

public class CloudgeneJob extends AbstractJob {

	private WdlMapReduce config;

	private String workingDirectory;

	private Executor executor;

	public static final int MAX_DOWNLOAD = 10;

	private static final Log log = LogFactory.getLog(CloudgeneJob.class);

	public CloudgeneJob() {
		super();
	}

	public void loadConfig(WdlMapReduce config) {

		this.config = config;
		workingDirectory = config.getPath();

		// set parameter properties that are not stored in database.
		// needed for restart
		for (CloudgeneParameter outputParam : outputParams) {

			for (WdlParameter output : config.getOutputs()) {

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

	public CloudgeneJob(User user, String id, WdlMapReduce config,
			Map<String, String> params) {

		this.config = config;
		setId(id);
		setUser(user);
		this.workingDirectory = config.getPath();

		// init parameters
		inputParams = new Vector<CloudgeneParameter>();
		for (WdlParameter input : config.getInputs()) {
			CloudgeneParameter newInput = new CloudgeneParameter(input);
			newInput.setJob(this);

			if (params.containsKey(input.getId())) {
				newInput.setValue(params.get(input.getId()));
			}

			inputParams.add(newInput);
		}

		outputParams = new Vector<CloudgeneParameter>();
		for (WdlParameter output : config.getOutputs()) {
			CloudgeneParameter newOutput = new CloudgeneParameter(output);
			newOutput.setJob(this);
			outputParams.add(newOutput);
		}

	}

	@Override
	public boolean setup() {

		context = new CloudgeneContext(this);
		// context.updateInputParameters();
		context.setupOutputParameters();

		return true;

	}

	@Override
	public boolean execute() {

		try {

			log.info("Job " + getId() + " submitted...");

			// evaluate WDL derictives
			Planner planner = new Planner();
			WdlApp app = planner.evaluateWDL(config, context);

			// create dag from wdl document
			Graph graph = planner.buildDAG(app.getMapred(), context);

			// execute optimzed dag
			executor = new Executor();
			boolean sccuessful = executor.execute(graph);

			if (!sccuessful) {
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
	public boolean executeSetup() {

		if (getState() == AbstractJob.STATE_CANCELED
				|| getState() == AbstractJob.STATE_FAILED) {
			onFailure();
			setStartTime(System.currentTimeMillis());
			setEndTime(System.currentTimeMillis());
			setError("Job Execution failed.");
			return false;
		}

		WdlStep step = getConfig().getSetup();

		if (step != null) {

			try {
				GraphNode node = new GraphNode(step, context);
				node.run();
				boolean result = node.isSuccessful();

				if (result) {
					return true;
				} else {
					setState(AbstractJob.STATE_FAILED);
					executeFailureStep(step);
					onFailure();
					setStartTime(System.currentTimeMillis());
					setEndTime(System.currentTimeMillis());
					setError("Job Execution failed.");
					return false;
				}

			} catch (Exception e) {
				setState(AbstractJob.STATE_FAILED);
				executeFailureStep(step);
				onFailure();
				setStartTime(System.currentTimeMillis());
				setEndTime(System.currentTimeMillis());
				e.printStackTrace();
				writeOutput(e.getMessage());
				setError(e.getMessage());
				return false;
			}

		} else {

			return true;

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

		WdlStep step = getConfig().getOnFailure();

		if (step != null) {

			try {
				writeLog("Executing onFailure... ");
				GraphNode node = new GraphNode(step, context);
				context.setData("cloudgene.failedStep", failedStep);
				context.setData("cloudgene.failedStep.classname",
						failedStep.getClassname());
				node.run();
				boolean result = node.isSuccessful();
				if (result) {

					// export parameters generated by onFailure step
					for (CloudgeneParameter out : getOutputParams()) {
						if (out.isAutoExport()
								&& step.getGenerates().contains(out.getName())) {
							log.info("Export parameter '" + out.getName()
									+ "'...");
							context.println("Export parameter '"
									+ out.getName() + "'...");
							exportParameter(out);
						}
					}

					writeLog("Executed onFailure successful.");
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

		// delete hdfs temp folders
		writeLog("Cleaning up temproary hdfs files...");
		HdfsUtil.delete(context.getHdfsTemp());

		// delete hdfs workspace
		if (isRemoveHdfsWorkspace()) {
			writeLog("Cleaning up hdfs files...");
			HdfsUtil.delete(context.getHdfsOutput());
			HdfsUtil.delete(context.getHdfsInput());
		}

		// delete local temp folders

		writeLog("Cleaning up uploaded local files...");
		//FileUtil.deleteDirectory(context.getLocalInput());
		
		writeLog("Cleaning up temproary local files...");
		FileUtil.deleteDirectory(context.getLocalTemp());

		return true;
	}

	@Override
	public boolean after() {

		// create output zip file for hdfs folders
		for (CloudgeneParameter out : getOutputParams()) {

			if (out.isDownload() && !out.isAutoExport()) {
				// export to local folder for faster download
				exportParameter(out);
			}

			writeLog("Exporting data successful.");

		}

		return true;
	}

	public boolean exportParameter(CloudgeneParameter out) {

		String localOutput = context.getLocalOutput();
		String workspace = getHdfsWorkspace();

		if (out.getType().equals(WdlParameter.HDFS_FOLDER)) {

			String localOutputDirectory = FileUtil.path(localOutput,
					out.getName());

			FileUtil.createDirectory(localOutputDirectory);

			String filename = context.getOutput(out.getName());
			String hdfsPath = null;
			if (filename.startsWith("hdfs://") || filename.startsWith("file:/")) {
				hdfsPath = filename;

			} else {

				hdfsPath = HdfsUtil.makeAbsolute(HdfsUtil.path(workspace,
						filename));
			}

			if (HdfsUtil.exists(hdfsPath)) {

				if (out.isZip()) {

					String zipName = FileUtil.path(localOutputDirectory,
							out.getName() + ".zip");

					if (out.isMergeOutput()) {

						HdfsUtil.compressAndMerge(zipName, hdfsPath,
								out.isRemoveHeader());

					} else {

						HdfsUtil.compress(zipName, hdfsPath);

					}

				} else {

					if (out.isMergeOutput()) {

						HdfsUtil.exportDirectoryAndMerge(localOutputDirectory,
								out.getName(), hdfsPath, out.isRemoveHeader());

					} else {

						HdfsUtil.exportDirectory(localOutputDirectory,
								out.getName(), hdfsPath);

					}

				}

			}

		}

		if (out.getType().equals(WdlParameter.HDFS_FILE)) {

			String localOutputDirectory = FileUtil.path(localOutput,
					out.getName());

			FileUtil.createDirectory(localOutputDirectory);

			String filename = context.getOutput(out.getName());
			String hdfsPath = null;
			if (filename.startsWith("hdfs://") || filename.startsWith("file:/")) {

				hdfsPath = filename;

			} else {

				hdfsPath = HdfsUtil.makeAbsolute(HdfsUtil.path(workspace,
						filename));
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

		String n = FileUtil.path(localOutput, out.getName());

		File f = new File(n);

		if (f.exists() && f.isDirectory()) {

			FileItem[] items = cloudgene.mapred.util.FileTree.getFileTree(
					localOutput, out.getName());

			List<Download> files = new Vector<Download>();

			for (FileItem item : items) {
				String hash = HashUtil.getMD5(item.getText() + item.getId()
						+ item.getSize() + getId() + (Math.random() * 100000));
				Download download = new Download();
				download.setName(item.getText());
				download.setPath(FileUtil.path(getId(), item.getId()));
				download.setSize(item.getSize());
				download.setHash(hash);
				download.setParameter(out);
				download.setCount(MAX_DOWNLOAD);
				files.add(download);
			}
			Collections.sort(files);
			out.setFiles(files);
		}

		return true;
	}

	public String getWorkingDirectory() {
		return workingDirectory;
	}

	public WdlMapReduce getConfig() {
		return config;
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
			setMap(executor.getMapProgress());
			setReduce(executor.getReduceProgress());

		} else {
			setMap(0);
			setReduce(0);
		}

	}

}
