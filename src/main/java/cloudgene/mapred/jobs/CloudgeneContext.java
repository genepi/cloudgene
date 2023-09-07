package cloudgene.mapred.jobs;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.sdk.WorkflowContext;
import cloudgene.mapred.util.MailUtil;
import cloudgene.mapred.util.Settings;
import genepi.io.FileUtil;

public class CloudgeneContext extends WorkflowContext {

	private String localTemp;

	private String localOutput;

	private String workingDirectory;

	private Settings settings;

	private CloudgeneStep step;

	private User user;

	private Map<String, CloudgeneParameterInput> inputParameters;

	private Map<String, CloudgeneParameterOutput> outputParameters;

	private Map<String, Integer> counters = new HashMap<String, Integer>();

	private Map<String, Boolean> submitCounters = new HashMap<String, Boolean>();

	private AbstractJob job;

	private Map<String, Object> data = new HashMap<String, Object>();

	private Map<String, Object> config;

	private int chunks = 0;

	public CloudgeneContext(CloudgeneJob job) {

		this.workingDirectory = job.getWorkingDirectory();
		this.job = job;

		this.user = job.getUser();

		setData("cloudgene.user.mail", user.getMail());
		setData("cloudgene.user.name", user.getFullName());

		localOutput = new File(job.getLocalWorkspace()).getAbsolutePath();
		localTemp = new File(FileUtil.path(job.getLocalWorkspace(), "temp")).getAbsolutePath();

		inputParameters = new HashMap<String, CloudgeneParameterInput>();
		for (CloudgeneParameterInput param : job.getInputParams()) {
			inputParameters.put(param.getName(), param);
		}

		outputParameters = new HashMap<String, CloudgeneParameterOutput>();
		for (CloudgeneParameterOutput param : job.getOutputParams()) {
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

	public String getPublicJobId() {
		return job.getPublicJobId();
	}

	public String getOutput(String param) {

		if (outputParameters.get(param) != null) {

			return outputParameters.get(param).getValue();

		} else {

			return null;

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

	public String getLocalTemp() {
		return localTemp;
	}

	public String getLocalOutput() {
		return localOutput;
	}

	public void println(String line) {
		job.writeOutputln(line);
	}

	public void log(String line, Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String sStackTrace = sw.toString(); // stack trace as a string
		job.writeLog(line + "\n" + sStackTrace);
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

		if (settings.getMail() != null) {

			MailUtil.send(settings.getMail().get("smtp"), settings.getMail().get("port"),
					settings.getMail().get("user"), settings.getMail().get("password"), settings.getMail().get("name"),
					user.getMail(), "[" + settings.getName() + "] " + subject, body);

		}

		return true;

	}

	public boolean sendMail(String to, String subject, String body) throws Exception {
		Settings settings = getSettings();

		if (settings.getMail() != null) {

			MailUtil.send(settings.getMail().get("smtp"), settings.getMail().get("port"),
					settings.getMail().get("user"), settings.getMail().get("password"), settings.getMail().get("name"),
					to, "[" + settings.getName() + "] " + subject, body);

		}
		return true;

	}

	@Override
	public boolean sendNotification(String text) throws Exception {
		return true;
	}

	public Set<String> getInputs() {
		return inputParameters.keySet();
	}

	public Set<String> getOutputs() {
		return outputParameters.keySet();
	}

	public void setInput(String input, String value) {

		CloudgeneParameterInput parameter = inputParameters.get(input);
		parameter.setValue(value);
	}

	public void setOutput2(String input, String value) {

		CloudgeneParameterOutput parameter = outputParameters.get(input);
		parameter.setValue(value);

	}

	public void incCounter(String name, int value) {

		Integer oldvalue = counters.get(name);
		if (oldvalue == null) {
			oldvalue = 0;
		}
		log("Increment counter " + name + " by " + value);
		counters.put(name, oldvalue + value);

	}

	public void submitCounter(String name) {
		log("Submit counter " + name);
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

		CloudgeneParameterOutput out = outputParameters.get(id);

		if (out != null) {

			return "<a href=\"/results/" + job.getId() + "/" + out.getName() + "/" + out.getName() + ".txt" + "\">"
					+ out.getName() + ".txt" + "</a>";

		} else {
			return "[PARAMETER UNKOWN!]";
		}

	}

	public String createLinkToFile(String id, String filename) {

		CloudgeneParameterOutput out = outputParameters.get(id);

		if (out != null) {

			return "<a href=\"/results/" + job.getId() + "/" + out.getName() + "/" + filename + "\">" + filename
					+ "</a>";

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

	public Message createTask(String name) {
		Message status = new Message(step, Message.RUNNING, name);

		List<Message> logs = step.getLogMessages();
		if (logs == null) {
			logs = new Vector<Message>();
			step.setLogMessages(logs);
		}
		logs.add(status);
		return status;
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

	@Override
	public void setConfig(Map<String, Object> config) {
		this.config = config;
	}

	@Override
	public String getConfig(String param) {
		if (config != null) {
			Object value = config.get(param);
			return value != null ? value.toString() : null;
		} else {
			return null;
		}
	}

	public void addFile(String filename) {
		chunks++;
		String chunkFolder = FileUtil.path(getLocalOutput(), "chunks");
		String chunkFilename = "chunk_" + chunks + ".html";
		FileUtil.createDirectory(chunkFolder);

		FileUtil.copy(filename, FileUtil.path(chunkFolder, chunkFilename));
		message(chunkFilename, 27);
	}

	@Override
	public String getHdfsTemp() {
		throw new RuntimeException("Not support in cg3");
	}

}
