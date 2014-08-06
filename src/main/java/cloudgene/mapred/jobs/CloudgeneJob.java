package cloudgene.mapred.jobs;

import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.database.CacheDao;
import cloudgene.mapred.jobs.cache.CacheDirectory;
import cloudgene.mapred.jobs.engine.Executor;
import cloudgene.mapred.jobs.engine.Planner;
import cloudgene.mapred.jobs.engine.graph.Graph;
import cloudgene.mapred.jobs.engine.graph.GraphNode;
import cloudgene.mapred.jobs.export.ExportJob;
import cloudgene.mapred.steps.importer.ImporterFactory;
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

	private Executor executor;

	private int MAX_DOWNLOAD = 10;

	private static final Log log = LogFactory.getLog(CloudgeneJob.class);

	public CloudgeneJob() {
		super();
	}

	public CloudgeneJob(WdlMapReduce config) throws Exception {

		this.config = config;

		// init parameters
		inputParams = new Vector<CloudgeneParameter>();
		for (WdlParameter input : config.getInputs()) {
			CloudgeneParameter newInput = new CloudgeneParameter(input);
			newInput.setJob(this);
			inputParams.add(newInput);
		}
		outputParams = new Vector<CloudgeneParameter>();
		for (WdlParameter output : config.getOutputs()) {
			CloudgeneParameter newOutput = new CloudgeneParameter(output);
			newOutput.setJob(this);
			outputParams.add(newOutput);
		}

		// init Cloudgene context
		context = new CloudgeneContext(config, this);
	}

	public void setInputParam(String id, String param) {

		for (int i = 0; i < inputParams.size(); i++) {
			CloudgeneParameter inputParam = inputParams.get(i);

			if (inputParam.getName().equalsIgnoreCase(id)) {

				if (isHdfsInput(i) && !ImporterFactory.needsImport(param)) {
					String workspace = Settings.getInstance().getHdfsWorkspace(
							getUser().getUsername());
					if (param != null && !param.isEmpty()) {
						if (HdfsUtil.isAbsolute(param)) {
							context.setInput(id, param);
						} else {
							String path = HdfsUtil.path(workspace, param);
							if (inputParam.isMakeAbsolute()) {
								context.setInput(id,
										HdfsUtil.makeAbsolute(path));
							} else {
								context.setInput(id, path);
							}
						}
					} else {

						context.setInput(id, "");

					}

				} else {
					context.setInput(id, param);
				}

				// change it..
				inputParam.setValue(context.getInput(id));

				return;
			}
		}

	}

	private void setOutputParam(String id, String param) {
		context.setOutput(id, param);
	}

	@Override
	public boolean setup() {

		// set output directory, temp directory & jobname
		String workspace = Settings.getInstance().getHdfsWorkspace(
				getUser().getUsername());

		String tempDirectory = HdfsUtil.makeAbsolute(HdfsUtil.path(workspace,
				"output", getId(), "temp"));

		String outputDirectory = HdfsUtil.makeAbsolute(HdfsUtil.path(workspace,
				"output", getId()));

		String localWorkspace = new File(Settings.getInstance()
				.getLocalWorkspace(getUser().getUsername())).getAbsolutePath();

		String localOutputDirectory = new File(FileUtil.path(localWorkspace,
				"output", getId())).getAbsolutePath();

		FileUtil.createDirectory(localOutputDirectory);

		String localTempDirectory = new File(FileUtil.path(localWorkspace,
				"output", getId(), "temp")).getAbsolutePath();

		FileUtil.createDirectory(localTempDirectory);

		// create output directories
		for (int i = 0; i < config.getOutputs().size(); i++) {

			WdlParameter param = config.getOutputs().get(i);
			if (param.getType().equals(WdlParameter.HDFS_FILE)
					| param.getType().equals(WdlParameter.HDFS_FOLDER)) {
				if (param.isTemp()) {
					setOutputParam(param.getId(),
							HdfsUtil.path(tempDirectory, param.getId()));
				} else {
					setOutputParam(param.getId(),
							HdfsUtil.path(outputDirectory, param.getId()));
				}
			}

			if (param.getType().equals(WdlParameter.LOCAL_FILE)) {
				FileUtil.createDirectory(FileUtil.path(localOutputDirectory,
						param.getId()));
				setOutputParam(param.getId(), FileUtil.path(
						localOutputDirectory, param.getId(), param.getId()));
			}

			if (param.getType().equals(WdlParameter.LOCAL_FOLDER)) {
				setOutputParam(param.getId(),
						FileUtil.path(localOutputDirectory, param.getId()));
				FileUtil.createDirectory(FileUtil.path(localOutputDirectory,
						param.getId()));
			}

		}

		// set output directory, temp directory & jobname

		context.setHdfsTemp(tempDirectory);
		context.setHdfsOutput(outputDirectory);
		context.setLocalTemp(localTempDirectory);
		context.setLocalOutput(localOutputDirectory);
		context.setUser(getUser());

		return true;
	}

	@Override
	public boolean execute() {

		try {

			log.info("job " + getId() + " submitted...");

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

			CacheDao dao = new CacheDao();
			CacheDirectory directory = new CacheDirectory(dao);

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
			executor = new Executor(directory);
			executor.setUseDag(dag);
			boolean sccuessful = executor.execute(graph);

			if (!sccuessful) {
				setError("Job Execution failed.");
				return false;
			}

			setError(null);
			return true;

		} catch (Exception e) {
			e.printStackTrace();
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
				return node.isSuccessful();
			} catch (Exception e) {
				e.printStackTrace();
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

		// delete hdfs folders
		String workspace = Settings.getInstance().getHdfsWorkspace(
				getUser().getUsername());

		String outputDirectory = HdfsUtil.makeAbsolute(HdfsUtil.path(workspace,
				"output", getId()));
		HdfsUtil.delete(outputDirectory);

		String tempDirectory = HdfsUtil.makeAbsolute(HdfsUtil.path(workspace,
				"temp", getId()));
		HdfsUtil.delete(tempDirectory);

		String inputDirectory = HdfsUtil.makeAbsolute(HdfsUtil.path(workspace,
				"input", getId()));
		HdfsUtil.delete(inputDirectory);

		return true;
	}

	@Override
	public boolean cleanUp() {

		// delete hdfs folders
		String workspace = Settings.getInstance().getHdfsWorkspace(
				getUser().getUsername());

		String outputDirectory = HdfsUtil.makeAbsolute(HdfsUtil.path(workspace,
				"output", getId()));
		HdfsUtil.delete(outputDirectory);

		String tempDirectory = HdfsUtil.makeAbsolute(HdfsUtil.path(workspace,
				"temp", getId()));
		HdfsUtil.delete(tempDirectory);

		String inputDirectory = HdfsUtil.makeAbsolute(HdfsUtil.path(workspace,
				"input", getId()));
		HdfsUtil.delete(inputDirectory);

		// delete local folder
		String localWorkspace = Settings.getInstance().getLocalWorkspace(
				getUser().getUsername());

		String localOutputDirectory = FileUtil.path(localWorkspace, "output",
				getId());

		FileUtil.deleteDirectory(localOutputDirectory);

		return false;
	}

	@Override
	public boolean after() {

		String workspace = Settings.getInstance().getHdfsWorkspace(
				getUser().getUsername());

		String localWorkspace = Settings.getInstance().getLocalWorkspace(
				getUser().getUsername());

		String localOutput = FileUtil.path(localWorkspace, "output", getId());

		if (!getUser().isExportToS3()) {
			// create output zip file for hdfs folders
			for (CloudgeneParameter out : getOutputParams()) {

				if (out.isDownload() && !out.isAutoExport()) {
					// export to local folder for faster download
					exportParameter(out);
				}
			}
		} else {

			// export to s3

			setS3Url("s3n://" + getUser().getS3Bucket() + "/" + getId());

			for (WdlParameter out : config.getOutputs()) {
				if (out.isDownload()) {
					copyParameterToS3(out);
				}

			}

			writeOutputln("Exporting data successful.");
		}

		// Delete temporary directory
		writeOutputln("Cleaning up temproary files...");
		String tempDirectory = HdfsUtil.makeAbsolute(HdfsUtil.path(workspace,
				"output", getId(), "temp"));

		HdfsUtil.delete(tempDirectory);

		if (Settings.getInstance().isRemoveHdfsWorkspace()) {

			writeOutputln("Cleaning up hdfs workspace files...");
			String outputDirectory = HdfsUtil.makeAbsolute(HdfsUtil.path(
					workspace, "output", getId()));
			HdfsUtil.delete(outputDirectory);

			String inputDirectory = HdfsUtil.makeAbsolute(HdfsUtil.path(
					workspace, "input", getId()));
			HdfsUtil.delete(inputDirectory);

		}

		writeOutputln("Clean up successful.");

		return true;
	}

	public boolean exportParameter(CloudgeneParameter out) {

		String localOutput = context.getLocalOutput();
		String workspace = Settings.getInstance().getHdfsWorkspace(
				getUser().getUsername());

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

			if (out.isZip()) {

				String zipName = FileUtil.path(localOutputDirectory,
						out.getId() + ".zip");

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

			if (out.isZip()) {

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

		String workspace = Settings.getInstance().getHdfsWorkspace(
				getUser().getUsername());

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
					log.error("Exporting data failed.");
					writeOutputln("Exporting data failed.");
				}

			} catch (Exception e) {

				log.error("Exporting data failed.", e);
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

	public WdlMapReduce getConfig() {
		return config;
	}

	private boolean isHdfsInput(int index) {
		return config.getInputs().get(index).getType()
				.equals(WdlParameter.HDFS_FILE)
				|| config.getInputs().get(index).getType()
						.equals(WdlParameter.HDFS_FOLDER);
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
