package cloudgene.mapred.jobs;

import genepi.io.FileUtil;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.core.User;
import cloudgene.mapred.util.S3Util;
import cloudgene.mapred.util.Settings;

abstract public class AbstractJob implements Runnable {

	private static final org.apache.commons.logging.Log log = LogFactory
			.getLog(AbstractJob.class);

	private DateFormat formatter = new SimpleDateFormat("yy/MM/dd HH:mm:ss");

	// types

	public static final int TYPE_MAPREDUCE = 1;

	public static final int TYPE_TASK = 2;

	public static final int TYPE_LOCAL = 3;

	// states

	public static final int STATE_WAITING = 1;

	public static final int STATE_RUNNING = 2;

	public static final int STATE_EXPORTING = 3;

	public static final int STATE_SUCCESS = 4;

	public static final int STATE_FAILED = 5;

	public static final int STATE_CANCELED = 6;

	public static final int STATE_RETIRED = 7;

	public static final int STATE_SUCESS_AND_NOTIFICATION_SEND = 8;

	public static final int STATE_FAILED_AND_NOTIFICATION_SEND = 9;

	// properties

	private String id;

	private int state = STATE_WAITING;

	private long startTime = 0;

	private long endTime = 0;

	private String name;

	private User user;

	private long deletedOn = -1;

	private String error = "";

	private String s3Url = "";

	private int map = -1;

	private int reduce = -1;

	private boolean setupComplete = false;

	private int positionInQueue = 0;

	protected List<CloudgeneParameter> inputParams = new Vector<CloudgeneParameter>();

	protected List<CloudgeneParameter> outputParams = new Vector<CloudgeneParameter>();

	protected List<CloudgeneStep> steps = new Vector<CloudgeneStep>();

	protected BufferedOutputStream stdOutStream;

	private BufferedOutputStream logStream;

	protected CloudgeneContext context;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public int getExecutionTime() {
		if (endTime == 0) {
			return (int) (System.currentTimeMillis() - startTime);
		} else {
			return (int) (endTime - startTime);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public void setS3Url(String s3Url) {
		this.s3Url = s3Url;
	}

	public String getS3Url() {
		return s3Url;
	}

	public void setMap(int map) {
		this.map = map;
	}

	public int getMap() {
		return map;
	}

	public void setReduce(int reduce) {
		this.reduce = reduce;
	}

	public int getReduce() {
		return reduce;
	}

	public void setDeletedOn(long deletedOn) {
		this.deletedOn = deletedOn;
	}

	public long getDeletedOn() {
		return deletedOn;
	}

	public List<CloudgeneParameter> getInputParams() {
		return inputParams;
	}

	public void setInputParams(List<CloudgeneParameter> inputParams) {
		this.inputParams = inputParams;
	}

	public List<CloudgeneParameter> getOutputParams() {
		return outputParams;
	}

	public void setOutputParams(List<CloudgeneParameter> outputParams) {
		this.outputParams = outputParams;
	}

	public void setPositionInQueue(int positionInQueue) {
		this.positionInQueue = positionInQueue;
	}

	public int getPositionInQueue() {
		return positionInQueue;
	}

	public void afterSubmission() {
		try {

			setup();

			initLocalDirectories();
			initStdOutFiles();

		} catch (Exception e1) {

			setEndTime(System.currentTimeMillis());

			setState(AbstractJob.STATE_FAILED);
			log.error("Job " + getId() + ": initialization failed.", e1);
			writeLog("Initialization failed: " + e1.getLocalizedMessage());
			return;

		}
	}

	@Override
	public void run() {

		log.info("Job " + getId() + ": running.");

		try {
			setState(AbstractJob.STATE_RUNNING);
			setStartTime(System.currentTimeMillis());
			writeLog("Details:");
			writeLog("  Name: " + getName());
			writeLog("  Job-Id: " + getId());
			writeLog("  Started At: " + getStartTime());
			writeLog("  Finished At: " + getExecutionTime());
			writeLog("  Execution Time: " + getExecutionTime());

			writeLog("  Inputs:");
			for (CloudgeneParameter parameter : inputParams) {
				writeLog("    " + parameter.getDescription() + ": "
						+ context.get(parameter.getName()));
			}

			writeLog("  Outputs:");
			for (CloudgeneParameter parameter : outputParams) {
				writeLog("    " + parameter.getDescription() + ": "
						+ context.get(parameter.getName()));
			}

			writeLog("Preparing Job....");
			boolean successfulBefore = before();

			if (!successfulBefore) {

				setState(AbstractJob.STATE_FAILED);
				log.error("Job " + getId() + ": job preparation failed.");
				writeLog("Job preparation failed.");

			} else {

				log.info("Job " + getId() + ": executing.");
				writeLog("Executing Job....");

				boolean succesfull = execute();

				if (succesfull) {

					log.info("Job " + getId() + ":  executed successful.");

					writeLog("Job executed successful.");
					writeLog("Exporting Data...");

					setState(AbstractJob.STATE_EXPORTING);

					try {

						boolean successfulAfter = after();

						if (successfulAfter) {

							setEndTime(System.currentTimeMillis());

							setState(AbstractJob.STATE_SUCCESS);
							log.info("Job " + getId()
									+ ": data export successful.");
							writeLog("Data export successful.");

						} else {

							setEndTime(System.currentTimeMillis());

							setState(AbstractJob.STATE_FAILED);
							log.error("Job " + getId()
									+ ": data export failed.");
							writeLog("Data export failed.");

						}

					} catch (Exception e) {

						setEndTime(System.currentTimeMillis());

						setState(AbstractJob.STATE_FAILED);
						log.error("Job " + getId() + ": data export failed.", e);
						writeLog("Data export failed: "
								+ e.getLocalizedMessage());

					}

				} else {

					setEndTime(System.currentTimeMillis());

					setState(AbstractJob.STATE_FAILED);
					log.error("Job " + getId() + ": execution failed. "
							+ getError());
					writeLog("Job execution failed: " + getError());

				}
			}

			if (getState() == AbstractJob.STATE_FAILED
					|| getState() == AbstractJob.STATE_CANCELED) {

				writeLog("Cleaning up...");
				onFailure();
				log.info("Job " + getId() + ": cleanup successful.");
				writeLog("Cleanup successful.");

			} else {
				writeLog("Cleaning up...");
				cleanUp();
				log.info("Job " + getId() + ": cleanup successful.");
				writeLog("Cleanup successful.");

			}

			closeStdOutFiles();

			exportStdOutToS3();
		} catch (Exception e1) {

			setEndTime(System.currentTimeMillis());

			setState(AbstractJob.STATE_FAILED);
			log.error("Job " + getId() + ": initialization failed.", e1);
			writeLog("Initialization failed: " + e1.getLocalizedMessage());

			writeLog("Cleaning up...");
			onFailure();
			log.info("Job " + getId() + ": cleanup successful.");
			writeLog("Cleanup successful.");

			return;

		}
	}

	public void cancel() {

		setEndTime(System.currentTimeMillis());

		writeLog("Canceled by user.");
		log.info("Job " + getId() + ": canceld by user.");

		/*
		 * if (state == STATE_RUNNING) { closeStdOutFiles(); }
		 */

		setState(AbstractJob.STATE_CANCELED);

	}

	private void initStdOutFiles() throws FileNotFoundException {

		stdOutStream = new BufferedOutputStream(new FileOutputStream(
				getStdOutFile()));

		logStream = new BufferedOutputStream(new FileOutputStream(
				getLogOutFile()));

	}

	private void initLocalDirectories() {

		if (getUser() != null) {

			String localWorkspace = Settings.getInstance().getLocalWorkspace(
					getUser().getUsername());

			String directory = FileUtil.path(localWorkspace, "output", getId());
			FileUtil.createDirectory(directory);

		}

	}

	public String getStdOutFile() {

		if (getUser() != null) {

			String localWorkspace = Settings.getInstance().getLocalWorkspace(
					getUser().getUsername());

			return FileUtil.path(localWorkspace, "output", getId(), "std.out");

		} else {

			return "";
		}
	}

	public String getLogOutFile() {

		if (getUser() != null) {

			String localWorkspace = Settings.getInstance().getLocalWorkspace(
					getUser().getUsername());

			return FileUtil.path(localWorkspace, "output", getId(), "job.txt");

		} else {

			return "";
		}
	}

	private void closeStdOutFiles() {

		try {

			stdOutStream.close();
			logStream.close();

		} catch (IOException e) {

		}

	}

	public void writeOutput(String line) {

		try {

			stdOutStream.write(line.getBytes());
			stdOutStream.flush();

		} catch (IOException e) {

		}

	}

	public void writeOutputln(String line) {

		try {

			stdOutStream.write((formatter.format(new Date()) + " ").getBytes());
			stdOutStream.write(line.getBytes());
			stdOutStream.write("\n".getBytes());
			stdOutStream.flush();

		} catch (IOException e) {
		}

	}

	public void writeLog(String line) {

		try {

			logStream.write((formatter.format(new Date()) + " ").getBytes());
			logStream.write(line.getBytes());
			logStream.write("\n".getBytes());
			logStream.flush();

		} catch (IOException e) {
		}

	}

	private void exportStdOutToS3() {

		// export to s3
		if (getUser().isExportToS3()) {

			S3Util.copyFile(getUser().getAwsKey(), getUser().getAwsSecretKey(),
					getUser().getS3Bucket(), getId(), getLogOutFile());

			S3Util.copyFile(getUser().getAwsKey(), getUser().getAwsSecretKey(),
					getUser().getS3Bucket(), getId(), getStdOutFile());
		}

	}

	public List<CloudgeneStep> getSteps() {
		return steps;
	}

	public void setSteps(List<CloudgeneStep> steps) {
		this.steps = steps;
	}

	public void setSetupComplete(boolean setupComplete) {
		this.setupComplete = setupComplete;
	}

	public boolean isSetupComplete() {
		return setupComplete;
	}

	public CloudgeneContext getContext() {
		return context;
	}

	abstract public boolean execute();

	abstract public boolean executeSetup();

	abstract public boolean setup();

	abstract public boolean before();

	abstract public boolean after();

	abstract public boolean onFailure();

	abstract public boolean cleanUp();

	abstract public boolean delete();

	abstract public int getType();

	public void kill() {

	}
}
