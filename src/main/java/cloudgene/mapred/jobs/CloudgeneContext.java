package cloudgene.mapred.jobs;

import genepi.hadoop.HdfsUtil;
import genepi.hadoop.common.WorkflowContext;
import genepi.io.FileUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import cloudgene.mapred.core.User;
import cloudgene.mapred.util.MailUtil;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlParameter;

public class CloudgeneContext extends WorkflowContext {

	private String hdfsTemp;

	private String localTemp;

	private String localInput;

	private String hdfsOutput;

	private String hdfsInput;

	private String localOutput;

	private String workingDirectory;

	private String workspace;

	private Settings settings;

	private CloudgeneStep step;

	private User user;

	private Map<String, CloudgeneParameter> inputParameters;

	private Map<String, CloudgeneParameter> outputParameters;

	private Map<String, Integer> counters = new HashMap<String, Integer>();

	private Map<String, Boolean> submitCounters = new HashMap<String, Boolean>();

	private AbstractJob job;

	private Map<String, Object> data = new HashMap<String, Object>();

	public CloudgeneContext(CloudgeneJob job) {

		this.workingDirectory = job.getWorkingDirectory();
		this.job = job;

		this.user = job.getUser();

		setData("cloudgene.user.mail", user.getMail());
		setData("cloudgene.user.name", user.getFullName());

		workspace = job.getHdfsWorkspace();

		hdfsTemp = HdfsUtil.makeAbsolute(HdfsUtil.path(workspace, "temp"));

		hdfsOutput = HdfsUtil.makeAbsolute(HdfsUtil.path(workspace));

		hdfsInput = HdfsUtil.makeAbsolute(HdfsUtil.path(workspace));

		localOutput = new File(job.getLocalWorkspace()).getAbsolutePath();

		localTemp = new File(FileUtil.path(job.getLocalWorkspace(), "temp")).getAbsolutePath();

		localInput = new File(FileUtil.path(job.getLocalWorkspace(), "input")).getAbsolutePath();

		inputParameters = new HashMap<String, CloudgeneParameter>();
		for (CloudgeneParameter param : job.getInputParams()) {
			inputParameters.put(param.getName(), param);
		}

		outputParameters = new HashMap<String, CloudgeneParameter>();
		for (CloudgeneParameter param : job.getOutputParams()) {
			outputParameters.put(param.getName(), param);
		}

		settings = job.getSettings();

	}

	public void setCurrentStep(CloudgeneStep currentStep) {
		this.step = currentStep;
	}

	public CloudgeneStep getCurrentStep() {
		return step;
	}

	public void setupOutputParameters() {

		// cleanup temp directories
		HdfsUtil.delete(getHdfsTemp());
		FileUtil.deleteDirectory(getLocalTemp());

		// create output directories
		FileUtil.createDirectory(getLocalOutput());
		FileUtil.createDirectory(getLocalTemp());

		// create output directories
		for (CloudgeneParameter param : outputParameters.values()) {

			switch (param.getType()) {
			case WdlParameter.HDFS_FILE:
			case WdlParameter.HDFS_FOLDER:

				String value = "";

				if (param.isDownload()) {
					value = HdfsUtil.path(getHdfsOutput(), param.getName());
				} else {
					value = HdfsUtil.path(getHdfsTemp(), param.getName());
				}

				if (!HdfsUtil.isAbsolute(value)) {
					value = HdfsUtil.makeAbsolute(value);
				}
				// delete (needed for restart)
				HdfsUtil.delete(value);
				param.setValue(value);
				break;

			// TODO: temp support for local files
			case WdlParameter.LOCAL_FILE:
				String folder = FileUtil.path(getLocalOutput(), param.getName());
				String filename = FileUtil.path(folder, param.getName());
				// delete and create (needed for restart)
				FileUtil.deleteDirectory(folder);
				FileUtil.createDirectory(folder);
				param.setValue(filename);
				break;

			case WdlParameter.LOCAL_FOLDER:
				String folder2 = FileUtil.path(getLocalOutput(), param.getName());
				// delete and create (needed for restart)
				FileUtil.deleteDirectory(folder2);
				FileUtil.createDirectory(folder2);
				param.setValue(folder2);
				break;
			}

		}

	}

	public String getInput(String param) {

		if (inputParameters.get(param) != null) {

			return inputParameters.get(param).getValue();

		} else {
			return null;
		}
	}

	public String getJobId() {
		return job.getId();
	}

	public String getOutput(String param) {

		if (outputParameters.get(param) != null) {

			return outputParameters.get(param).getValue();

		} else {

			return outputParameters.get(param).getValue();

		}

	}

	public String get(String param) {
		String result = getInput(param);
		if (result == null) {
			return getOutput(param);
		} else {
			return result;
		}
	}

	public Settings getSettings() {
		return settings;
	}

	public String getHdfsTemp() {
		return hdfsTemp;
	}

	public String getLocalTemp() {
		return localTemp;
	}

	public String getLocalInput() {
		return localInput;
	}

	public String getHdfsOutput() {
		return hdfsOutput;
	}

	public String getHdfsInput() {
		return hdfsInput;
	}

	public String getLocalOutput() {
		return localOutput;
	}

	public void println(String line) {
		job.writeOutputln(line);
	}

	public void log(String line) {
		job.writeLog(line);
	}

	public AbstractJob getJob() {
		return job;
	}

	@Override
	public String getJobName() {
		return job.getName();
	}

	public String getWorkingDirectory() {
		return workingDirectory;
	}

	public User getUser() {
		return user;
	}

	public boolean sendMail(String subject, String body) throws Exception {
		Settings settings = getSettings();

		MailUtil.send(settings.getMail().get("smtp"), settings.getMail().get("port"), settings.getMail().get("user"),
				settings.getMail().get("password"), settings.getMail().get("name"), user.getMail(),
				"[" + settings.getName() + "] " + subject, body);

		return true;

	}

	public boolean sendMail(String to, String subject, String body) throws Exception {
		Settings settings = getSettings();

		MailUtil.send(settings.getMail().get("smtp"), settings.getMail().get("port"), settings.getMail().get("user"),
				settings.getMail().get("password"), settings.getMail().get("name"), to,
				"[" + settings.getName() + "] " + subject, body);

		return true;

	}

	public Set<String> getInputs() {
		return inputParameters.keySet();
	}

	public Set<String> getOutputs() {
		return outputParameters.keySet();
	}

	public void setInput(String input, String value) {

		CloudgeneParameter parameter = inputParameters.get(input);
		parameter.setValue(value);
	}

	public void setOutput(String input, String value) {

		CloudgeneParameter parameter = outputParameters.get(input);
		parameter.setValue(value);

	}

	public void incCounter(String name, int value) {

		Integer oldvalue = counters.get(name);
		if (oldvalue == null) {
			oldvalue = 0;
		}
		counters.put(name, oldvalue + value);

	}

	public void submitCounter(String name) {

		submitCounters.put(name, true);
	}

	public Map<String, Integer> getSubmittedCounters() {
		Map<String, Integer> result = new HashMap<String, Integer>();
		for (String counter : submitCounters.keySet()) {
			result.put(counter, counters.get(counter));
		}
		return result;
	}

	public Map<String, Integer> getCounters() {
		return counters;
	}

	public String createLinkToFile(String id) {

		CloudgeneParameter out = outputParameters.get(id);

		if (out != null) {

			return "<a href=\"/results/" + job.getId() + "/" + out.getName() + "/" + out.getName() + ".txt" + "\">"
					+ out.getName() + ".txt" + "</a>";

		} else {
			return "[PARAMETER UNKOWN!]";
		}

	}

	public void message(String message, int type) {
		Message status = new Message(step, type, message);

		List<Message> logs = step.getLogMessages();
		if (logs == null) {
			logs = new Vector<Message>();
			step.setLogMessages(logs);
		}
		logs.add(status);

	}

	public void beginTask(String name) {
		Message status = new Message(step, Message.RUNNING, name);

		List<Message> logs = step.getLogMessages();
		if (logs == null) {
			logs = new Vector<Message>();
			step.setLogMessages(logs);
		}
		logs.add(status);
	}

	public void beginTask(String name, int totalWork) {
		beginTask(name);
	}

	public void endTask(String message, int type) {
		Message status = step.getLogMessages().get(step.getLogMessages().size() - 1);
		status.setType(type);
		status.setMessage(message);
	}

	public void updateTask(String message, int type) {
		Message status = step.getLogMessages().get(step.getLogMessages().size() - 1);
		status.setType(type);
		status.setMessage(message);
	}

	public void updateTask(String message) {
		Message status = step.getLogMessages().get(step.getLogMessages().size() - 1);
		status.setMessage(message);
	}

	public void endTask(int type) {
		Message status = step.getLogMessages().get(step.getLogMessages().size() - 1);
		status.setType(type);
	}

	public Object getData(String key) {
		return data.get(key);
	}

	public void setData(String key, Object object) {
		data.put(key, object);
	}

}
