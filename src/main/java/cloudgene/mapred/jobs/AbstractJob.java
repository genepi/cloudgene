package cloudgene.mapred.jobs;

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
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloudgene.mapred.apps.Application;
import cloudgene.mapred.apps.ApplicationRepository;
import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.queue.PriorityRunnable;
import cloudgene.mapred.jobs.workspace.IWorkspace;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlParameterInputType;
import genepi.io.FileUtil;

abstract public class AbstractJob extends PriorityRunnable {

	private static final Logger log = LoggerFactory.getLogger(AbstractJob.class);

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

	public static final int STATE_DELETED = 10;

	// properties

	private String id;

	private int state = STATE_WAITING;

	private long startTime = 0;

	private long endTime = 0;

	private long setupStartTime = 0;

	private long setupEndTime = 0;

	private long submittedOn = 0;

	private long finishedOn = 0;

	private String name;

	private User user;

	private String userAgent = "";

	private long deletedOn = -1;

	private String application;

	private String applicationId;

	private String error = "";

	private int progress = -1;

	private boolean setupComplete = false;

	private boolean setupRunning = false;

	private boolean complete = true;

	private int positionInQueue = -1;

	protected List<CloudgeneParameterInput> inputParams = new Vector<CloudgeneParameterInput>();

	protected List<CloudgeneParameterOutput> outputParams = new Vector<CloudgeneParameterOutput>();

	protected List<CloudgeneStep> steps = new Vector<CloudgeneStep>();

	protected BufferedOutputStream stdOutStream;

	private BufferedOutputStream logStream;

	protected CloudgeneContext context;

	private Settings settings;

	private String logs;

	private String localWorkspace;

	private boolean canceld = false;

	protected IWorkspace workspace;

	private String workspaceSize = null;

	private String publicJobId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
		this.publicJobId = HashUtil.getSha256(id + RandomStringUtils.random(500));
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

	public void setSetupStartTime(long setupStartTime) {
		this.setupStartTime = setupStartTime;
	}

	public long getSetupStartTime() {
		return setupStartTime;
	}

	public void setSetupEndTime(long setupEndTime) {
		this.setupEndTime = setupEndTime;
	}

	public long getSetupEndTime() {
		return setupEndTime;
	}

	public void setSubmittedOn(long submitedOn) {
		this.submittedOn = submitedOn;
	}

	public long getSubmittedOn() {
		return submittedOn;
	}

	public void setFinishedOn(long finishedOn) {
		this.finishedOn = finishedOn;
	}

	public long getFinishedOn() {
		return finishedOn;
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

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public int getProgress() {
		return progress;
	}

	public void setDeletedOn(long deletedOn) {
		this.deletedOn = deletedOn;
	}

	public long getDeletedOn() {
		return deletedOn;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public List<CloudgeneParameterInput> getInputParams() {
		return inputParams;
	}

	public void setInputParams(List<CloudgeneParameterInput> inputParams) {
		this.inputParams = inputParams;
	}

	public List<CloudgeneParameterOutput> getOutputParams() {
		return outputParams;
	}

	public void setOutputParams(List<CloudgeneParameterOutput> outputParams) {
		this.outputParams = outputParams;
	}

	public void setPositionInQueue(int positionInQueue) {
		this.positionInQueue = positionInQueue;
	}

	public int getPositionInQueue() {
		return positionInQueue;
	}

	public void setWorkspaceSize(String workspaceSize) {
		this.workspaceSize = workspaceSize;
	}

	public String getWorkspaceSize() {
		return workspaceSize;
	}

	public boolean afterSubmission() {
		try {

			initStdOutFiles();

			setup();

			return true;

		} catch (Exception e1) {

			// setEndTime(System.currentTimeMillis());

			setState(AbstractJob.STATE_FAILED);
			log.error("Job " + getId() + ": initialization failed.", e1);
			writeLog("Initialization failed: " + e1.getLocalizedMessage());
			setSetupComplete(false);
			state = AbstractJob.STATE_FAILED;
			return false;

		}
	}

	public void runInstallationAndResolveAppLinks() {

		Settings settings = getSettings();
		ApplicationRepository repository = settings.getApplicationRepository();

		// resolve application links
		for (CloudgeneParameterInput input : getInputParams()) {
			if (input.getType() == WdlParameterInputType.APP_LIST) {
				String value = input.getValue();
				String linkedAppId = value;
				if (value.startsWith("apps@")) {
					linkedAppId = value.replaceAll("apps@", "");
				}

				if (value.isEmpty()) {
					continue;
				}
				Application linkedApp = repository.getByIdAndUser(linkedAppId, getUser());
				if (linkedApp == null) {
					String error = "Application " + linkedAppId + " is not installed or wrong permissions.";
					log.info(error);
					writeOutput(error);
					setError(error);
					setSetupComplete(false);
					setState(AbstractJob.STATE_FAILED);
					return;
				}
				// update environment variables
				Environment environment = settings.buildEnvironment().addApplication(linkedApp.getWdlApp())
						.addContext(context);
				Map<String, Object> properties = linkedApp.getWdlApp().getProperties();
				for (String property : properties.keySet()) {
					Object propertyValue = properties.get(property);
					if (propertyValue instanceof String) {
						propertyValue = environment.resolve(propertyValue.toString());
					}
					properties.put(property, propertyValue);
				}

				getContext().setData(input.getName(), properties);

			}
		}

		setSetupComplete(true);

	}

	@Override
	public void run() {

		if (isCanceld()) {
			return;
		}

		log.info("[Job {}] Setup job...", getId());
		setState(AbstractJob.STATE_RUNNING);

		setSetupStartTime(System.currentTimeMillis());
		setSetupRunning(true);
		runInstallationAndResolveAppLinks();
		setSetupEndTime(System.currentTimeMillis());
		setSetupRunning(false);
		if (!isSetupComplete()) {
			log.info("[Job {}] Setup failed.", getId());
			setFinishedOn(System.currentTimeMillis());
			setComplete(true);
			return;
		}

		log.info("[Job {}] Running job...", getId());
		setStartTime(System.currentTimeMillis());

		try {
			writeLog("Details:");
			writeLog("  Name: " + getName());
			writeLog("  Job-Id: " + getId());
			writeLog("  Submitted On: " + new Date(getSubmittedOn()).toString());
			writeLog("  Submitted By: " + getUser().getUsername());
			writeLog("  User-Agent: " + getUserAgent());
			writeLog("  Inputs:");
			for (CloudgeneParameterInput parameter : inputParams) {
				writeLog("    " + parameter.getDescription() + ": " + context.get(parameter.getName()));
			}

			// TODO: check if all input parameters are set

			writeLog("  Outputs:");
			for (CloudgeneParameterOutput parameter : outputParams) {
				writeLog("    " + parameter.getDescription() + ": " + context.get(parameter.getName()));
			}

			writeLog("Executing Job....");

			boolean succesfull = execute();

			if (succesfull) {

				log.info("[Job {}] Execution successful.", getId());

				writeLog("Job Execution successful.");
				writeLog("Exporting Data...");

				setState(AbstractJob.STATE_EXPORTING);

				try {

					boolean successfulAfter = after();

					if (successfulAfter) {

						setState(AbstractJob.STATE_SUCCESS);
						log.info("[Job {}]  data export successful.", getId());
						writeLog("Data Export successful.");

					} else {

						setState(AbstractJob.STATE_FAILED);
						log.error("[Job {}]  data export failed.", getId());
						writeLog("Data Export failed.");

					}

				} catch (Error | Exception e) {

					Writer writer = new StringWriter();
					PrintWriter printWriter = new PrintWriter(writer);
					e.printStackTrace(printWriter);
					String s = writer.toString();

					setState(AbstractJob.STATE_FAILED);
					log.error("[Job {}]  data export failed.", getId(), e);
					writeLog("Data Export failed: " + e.getLocalizedMessage() + "\n" + s);

				}

			} else {

				setState(AbstractJob.STATE_FAILED);
				log.error("[Job {}] Execution failed. {}", getId(), getError());
				writeLog("Job Execution failed: " + getError());

			}

			if (getState() == AbstractJob.STATE_FAILED || getState() == AbstractJob.STATE_CANCELED) {

				writeLog("Cleaning up...");
				onFailure();
				log.info("[Job {}]cleanup successful.", getId());
				writeLog("Cleanup successful.");

			} else {
				writeLog("Cleaning up...");
				cleanUp();
				log.info("[Job {}] cleanup successful.", getId());
				writeLog("Cleanup successful.");

			}

			if (canceld) {
				setState(AbstractJob.STATE_CANCELED);
			}

			closeStdOutFiles();

			setEndTime(System.currentTimeMillis());

		} catch (Exception | Error e) {

			setState(AbstractJob.STATE_FAILED);
			log.error("[Job {}]: initialization failed.", getId(), e);

			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			e.printStackTrace(printWriter);
			String s = writer.toString();

			writeLog("Initialization failed: " + e.getLocalizedMessage() + "\n" + s);

			writeLog("Cleaning up...");
			onFailure();
			log.info("[Job {}]: cleanup successful.", getId());
			writeLog("Cleanup successful.");

			closeStdOutFiles();

			setEndTime(System.currentTimeMillis());

		}
	}

	public void cancel() {

		writeLog("Canceled by user.");
		log.info("[Job {}]: canceld by user.", getId());

		/*
		 * if (state == STATE_RUNNING) { closeStdOutFiles(); }
		 */
		canceld = true;
		setState(AbstractJob.STATE_CANCELED);

	}

	private void initStdOutFiles() throws FileNotFoundException {

		// if (stdOutStream == null) {
		stdOutStream = new BufferedOutputStream(new FileOutputStream(FileUtil.path(localWorkspace, "std.out")));

		// }
		// if (logStream == null) {
		logStream = new BufferedOutputStream(new FileOutputStream(FileUtil.path(localWorkspace, "job.txt")));
		// }

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
			if (stdOutStream != null && line != null) {
				stdOutStream.write(line.getBytes("UTF-8"));
				stdOutStream.flush();

			}
		} catch (IOException e) {

		}

	}

	public void writeOutputln(String line) {

		try {
			if (stdOutStream == null) {
				initStdOutFiles();
			}

			stdOutStream.write(line.getBytes("UTF-8"));
			stdOutStream.write("\n".getBytes("UTF-8"));
			stdOutStream.flush();

		} catch (IOException e) {
		}

	}

	public void writeLog(String line) {

		try {
			if (logStream == null) {
				initStdOutFiles();
			}

			logStream.write((formatter.format(new Date()) + " ").getBytes());
			logStream.write(line.getBytes("UTF-8"));
			logStream.write("\n".getBytes("UTF-8"));
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

	public void setSetupRunning(boolean setupRunning) {
		this.setupRunning = setupRunning;
	}

	public boolean isSetupRunning() {
		return setupRunning;
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

	public String getLocalWorkspace() {
		return localWorkspace;
	}

	public void setLocalWorkspace(String localWorkspace) {
		this.localWorkspace = localWorkspace;
	}

	public void setWorkspace(IWorkspace workspace) {
		this.workspace = workspace;
	}

	public IWorkspace getWorkspace() {
		return workspace;
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

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	public boolean isComplete() {
		return this.complete;
	}

	public boolean isCanceld() {
		return canceld;
	}

	public boolean isRunning() {
		return !complete;
	}

	abstract public boolean execute();

	abstract public boolean setup();

	abstract public boolean after();

	abstract public boolean onFailure();

	abstract public boolean cleanUp();

	public void kill() {

	}

	public long getCurrentTime() {
		return System.currentTimeMillis();
	}

	public void setCurrentTime(long time) {

	}

	public String getPublicJobId() {
		return publicJobId;
	}

}
