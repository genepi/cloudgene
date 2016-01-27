package cloudgene.mapred.jobs;

import genepi.io.FileUtil;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.core.User;
import cloudgene.mapred.util.Settings;

abstract public class AbstractJob implements Runnable {

	private static final org.apache.commons.logging.Log log = LogFactory
			.getLog(AbstractJob.class);

	private DateFormat formatter = new SimpleDateFormat("yy/MM/dd HH:mm:ss");

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

	public static final int STATE_DEAD = -1;

	// properties

	private String id;

	private int state = STATE_WAITING;

	private long startTime = 0;

	private long endTime = 0;

	private String name;

	private User user;

	private long deletedOn = -1;

	private String application;

	private String applicationId;

	private String error = "";

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

	private Settings settings;

	private String logs;

	private boolean removeHdfsWorkspace;

	private String localWorkspace;

	private String hdfsWorkspace;

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

			initStdOutFiles();

			setup();

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

		if (state == AbstractJob.STATE_CANCELED
				|| state == AbstractJob.STATE_FAILED) {
			return;
		}

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

			// TODO: check if all input parameters are set

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

						Writer writer = new StringWriter();
						PrintWriter printWriter = new PrintWriter(writer);
						e.printStackTrace(printWriter);
						String s = writer.toString();

						setState(AbstractJob.STATE_FAILED);
						log.error("Job " + getId() + ": data export failed.", e);
						writeLog("Data export failed: "
								+ e.getLocalizedMessage() + "\n" + s);

					} catch (Error e) {

						setEndTime(System.currentTimeMillis());

						Writer writer = new StringWriter();
						PrintWriter printWriter = new PrintWriter(writer);
						e.printStackTrace(printWriter);
						String s = writer.toString();

						setState(AbstractJob.STATE_FAILED);
						log.error("Job " + getId() + ": data export failed.", e);
						writeLog("Data export failed: "
								+ e.getLocalizedMessage() + "\n" + s);

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

		} catch (Exception e1) {

			setEndTime(System.currentTimeMillis());

			setState(AbstractJob.STATE_FAILED);
			log.error("Job " + getId() + ": initialization failed.", e1);

			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			e1.printStackTrace(printWriter);
			String s = writer.toString();

			writeLog("Initialization failed: " + e1.getLocalizedMessage()
					+ "\n" + s);

			writeLog("Cleaning up...");
			onFailure();
			log.info("Job " + getId() + ": cleanup successful.");
			writeLog("Cleanup successful.");

			return;

		} catch (Error e) {

			setEndTime(System.currentTimeMillis());

			setState(AbstractJob.STATE_FAILED);
			log.error("Job " + getId() + ": initialization failed.", e);

			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			e.printStackTrace(printWriter);
			String s = writer.toString();

			writeLog("Initialization failed: " + e.getLocalizedMessage() + "\n"
					+ s);

			writeLog("Cleaning up...");
			onFailure();
			log.info("Job " + getId() + ": cleanup successful.");
			writeLog("Cleanup successful.");

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

	public String getStdOutFile() {

		return FileUtil.path(localWorkspace, "std.out");
	}

	public String getLogOutFile() {

		return FileUtil.path(localWorkspace, "job.txt");
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

	public void setLogs(String logs) {
		this.logs = logs;
	}

	public String getLogs() {
		return logs;
	}

	@Override
	public boolean equals(Object obj) {

		if (!(obj instanceof AbstractJob)) {
			return false;
		}

		return ((AbstractJob) obj).getId().equals(id);

	}

	public Settings getSettings() {
		return settings;
	}

	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	public boolean isRemoveHdfsWorkspace() {
		return removeHdfsWorkspace;
	}

	public void setRemoveHdfsWorkspace(boolean removeHdfsWorkspace) {
		this.removeHdfsWorkspace = removeHdfsWorkspace;
	}

	public String getLocalWorkspace() {
		return localWorkspace;
	}

	public void setLocalWorkspace(String localWorkspace) {
		this.localWorkspace = localWorkspace;
	}

	public String getHdfsWorkspace() {
		return hdfsWorkspace;
	}

	public void setHdfsWorkspace(String hdfsWorkspace) {
		this.hdfsWorkspace = hdfsWorkspace;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getApplication() {
		return application;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public boolean isRunning() {
		return (state == STATE_WAITING) || (state == STATE_RUNNING)
				|| (state == STATE_EXPORTING);
	}

	abstract public boolean execute();

	abstract public boolean executeSetup();

	abstract public boolean setup();

	abstract public boolean before();

	abstract public boolean after();

	abstract public boolean onFailure();

	abstract public boolean cleanUp();

	public void kill() {

	}

}
