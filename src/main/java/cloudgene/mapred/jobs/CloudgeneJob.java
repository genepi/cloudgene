package cloudgene.mapred.jobs;

import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.CacheDao;
import cloudgene.mapred.jobs.cache.CacheDirectory;
import cloudgene.mapred.jobs.engine.Executor;
import cloudgene.mapred.jobs.engine.Planner;
import cloudgene.mapred.jobs.engine.graph.Graph;
import cloudgene.mapred.jobs.engine.graph.GraphNode;
import cloudgene.mapred.jobs.export.ExportJob;
import cloudgene.mapred.util.FileItem;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.S3Util;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlMapReduce;
import cloudgene.mapred.wdl.WdlParameter;
import cloudgene.mapred.wdl.WdlStep;

public class CloudgeneJob extends AbstractJob {

	private WdlMapReduce config;

	private String workingDirectory;

	private Executor executor;

	private int MAX_DOWNLOAD = 10;

	private static final Log log = LogFactory.getLog(CloudgeneJob.class);

	public CloudgeneJob() {
		super();

	}

	public void loadConfig(WdlMapReduce config) {

		this.config = config;
		workingDirectory = config.getPath();

	}

	public CloudgeneJob(User user, String id, WdlMapReduce config,
			Map<String, String> params) throws Exception {
	
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
		context.updateInputParameters();
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

			// needs importerStep??
			/*
			 * boolean needsImporter = false; for (CloudgeneParameter param :
			 * getInputParams()) { if
			 * (ImporterFactory.needsImport(param.getValue())) { needsImporter =
			 * true; } }
			 * 
			 * if (needsImporter) {
			 * 
			 * WdlStep importerStep = new WdlStep();
			 * importerStep.setName("Data Import"); importerStep
			 * .setClassname("cloudgene.mapred.steps.HdfsImporter");
			 * app.getMapred().getSteps().add(0, importerStep);
			 * 
			 * }
			 */

			boolean dag = config.getType().equals("dag");

			// create dag from wdl document
			Graph graph = planner.buildDAG(app.getMapred(), context);

			// load cache

			// CacheDao dao = new CacheDao();
			// CacheDirectory directory = new CacheDirectory(dao);

			// optimize dag
			/*
			 * Optimizer optimizer = new Optimizer(directory);
			 * optimizer.optimize(graph);
			 * 
			 * writeLog("  Outputs:"); for (CloudgeneParameter parameter :
			 * outputParams) { writeLog("    " + parameter.getDescription() +
			 * ": " + parameter.getValue()); }
			 */

			// execute optimzed dag
			executor = new Executor();
			executor.setUseDag(dag);
			boolean sccuessful = executor.execute(graph);

			if (!sccuessful) {
				setError("Job Execution failed.");
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
					onFailure();
					setStartTime(System.currentTimeMillis());
					setEndTime(System.currentTimeMillis());
					setError("Job Execution failed.");
					return false;
				}

			} catch (Exception e) {
				setState(AbstractJob.STATE_FAILED);
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

			if (HdfsUtil.exists(localOutputDirectory)) {

				FileUtil.createDirectory(localOutputDirectory);

				String filename = context.getOutput(out.getName());
				String hdfsPath = null;
				if (filename.startsWith("hdfs://")
						|| filename.startsWith("file:/")) {
					hdfsPath = filename;

				} else {

					hdfsPath = HdfsUtil.makeAbsolute(HdfsUtil.path(workspace,
							filename));
				}

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

				HdfsUtil.exportFile(localOutputDirectory, hdfsPath);

			} else {

				HdfsUtil.compressFile(localOutputDirectory, hdfsPath);

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
						+ item.getSize() + getId());
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

	public boolean copyParameterToS3(WdlParameter out) {
		String filename = context.getOutput(out.getId());

		String workspace = getHdfsWorkspace();

		if (out.getType().equals(WdlParameter.HDFS_FOLDER)
				|| out.getType().equals(WdlParameter.HDFS_FILE)) {

			String hdfsPath = null;
			if (filename.startsWith("hdfs://")) {
				hdfsPath = filename;
			} else {
				hdfsPath = HdfsUtil.path(workspace, filename);
			}

			/** set job specific attributes */
			try {

				ExportJob copyJob = new ExportJob("Copy data to s3");
				copyJob.setInput(hdfsPath);
				copyJob.setAwsKey(getUser().getAwsKey());
				copyJob.setAwsSecretKey(getUser().getAwsSecretKey());
				copyJob.setS3Bucket(getUser().getS3Bucket());
				copyJob.setDirectory(FileUtil.path(getId(), out.getId()));
				copyJob.setOutput(hdfsPath + "_temp");
				writeOutputln("Copy data from " + hdfsPath + " to s3n://"
						+ getUser().getS3Bucket() + "/"
						+ FileUtil.path(getId(), out.getId()));

				boolean success = copyJob.execute();

				if (!success) {
					log.error("Job " + getId() + ": Exporting data failed.");
					writeOutputln("Exporting data failed.");
				}

			} catch (Exception e) {

				log.error("Job " + getId() + ": Exporting data failed.", e);
				writeOutputln("Exporting data failed. " + e.getMessage());
			}
		}

		if (out.getType().equals(WdlParameter.LOCAL_FILE)) {

			S3Util.copyFile(getUser().getAwsKey(), getUser().getAwsSecretKey(),
					getUser().getS3Bucket(), getId(), filename);
		}

		if (out.getType().equals(WdlParameter.LOCAL_FOLDER)) {

			S3Util.copyDirectory(getUser().getAwsKey(), getUser()
					.getAwsSecretKey(), getUser().getS3Bucket(), getId(),
					filename);
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
	public int getType() {

		return AbstractJob.TYPE_MAPREDUCE;

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
