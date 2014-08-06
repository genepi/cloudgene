package cloudgene.mapred.jobs;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.CounterDao;
import cloudgene.mapred.util.MailUtil;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlMapReduce;

public class CloudgeneContext {

	private String hdfsTemp;
	private String localTemp;
	private String hdfsOutput;
	private String localOutput;
	private String workingDirectory;

	private User user;

	private Map<String, String> inputValues;
	private Map<String, String> outputValues;
	private Map<String, CloudgeneParameter> parameters;

	private Map<String, Integer> counters = new HashMap<String, Integer>();

	private AbstractJob job;

	public CloudgeneContext(WdlMapReduce config, AbstractJob job) {

		this.workingDirectory = config.getPath();
		this.job = job;

		inputValues = new HashMap<String, String>();
		for (int i = 0; i < config.getInputs().size(); i++) {
			inputValues.put(config.getInputs().get(i).getId(), "");
		}

		outputValues = new HashMap<String, String>();
		for (int i = 0; i < config.getOutputs().size(); i++) {
			outputValues.put(config.getOutputs().get(i).getId(), "");
		}

		parameters = new HashMap<String, CloudgeneParameter>();
		for (CloudgeneParameter param : job.getInputParams()) {
			parameters.put(param.getName(), param);
		}
		for (CloudgeneParameter param : job.getOutputParams()) {
			parameters.put(param.getName(), param);
		}

	}

	public String getInput(String param) {
		return inputValues.get(param);
	}

	public String getOutput(String param) {
		return outputValues.get(param);
	}

	public CloudgeneParameter getParameter(String id) {
		return parameters.get(id);
	}

	public String get(String param) {
		String result = getInput(param);
		if (result == null) {
			return getOutput(param);
		} else {
			return result;
		}
	}

	public String getHdfsTemp() {
		return hdfsTemp;
	}

	public void setHdfsTemp(String hdfsTemp) {
		this.hdfsTemp = hdfsTemp;
	}

	public String getLocalTemp() {
		return localTemp;
	}

	public void setLocalTemp(String localTemp) {
		this.localTemp = localTemp;
	}

	public String getHdfsOutput() {
		return hdfsOutput;
	}

	public void setHdfsOutput(String hdfsOutput) {
		this.hdfsOutput = hdfsOutput;
	}

	public String getLocalOutput() {
		return localOutput;
	}

	public void setLocalOutput(String localOutput) {
		this.localOutput = localOutput;
	}

	public Map<String, CloudgeneParameter> getParameters() {
		return parameters;
	}

	public void worked(int work) {

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

	public String getWorkingDirectory() {
		return workingDirectory;
	}

	public void setWorkingDirectory(String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

	public boolean sendMail(String subject, String body) throws Exception {
		Settings settings = Settings.getInstance();

		MailUtil.send(settings.getMail().get("smtp"),
				settings.getMail().get("port"), settings.getMail().get("user"),
				settings.getMail().get("password"),
				settings.getMail().get("name"), user.getMail(),
				"[" + settings.getName() + "] " + subject, body);

		return true;

	}

	public Set<String> getInputs() {
		return inputValues.keySet();
	}

	public void setInput(String input, String value) {

		inputValues.put(input, value);

	}

	public void setOutput(String input, String value) {

		outputValues.put(input, value);
		CloudgeneParameter parameter = parameters.get(input);
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

		Integer value = counters.get(name);

		if (value != null) {

			CounterDao dao = new CounterDao();
			dao.insert(name, value, job);

		}
	}

	public Map<String, Integer> getCounters() {
		return counters;
	}

	public String createLinkToFile(String id) {

		CloudgeneParameter out = getParameter(id);

		if (out != null) {

			return "<a href=\"/results/" + job.getId() + "/" + out.getName()
					+ "/" + out.getName() + ".txt" + "\">" + out.getName()
					+ ".txt" + "</a>";

		} else {
			return "[PARAMETER UNKOWN!]";
		}

	}

}
