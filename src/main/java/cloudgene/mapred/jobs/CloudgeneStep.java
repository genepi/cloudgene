package cloudgene.mapred.jobs;

import java.io.File;
import java.util.List;
import java.util.Vector;

import cloudgene.mapred.wdl.WdlStep;

public abstract class CloudgeneStep {

	private int id;

	private String name;

	private AbstractJob job;

	private List<Message> logMessages;

	public String getFolder(Class clazz) {
		return new File(clazz.getProtectionDomain().getCodeSource()
				.getLocation().getPath()).getParent();
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public AbstractJob getJob() {
		return job;
	}

	public void setJob(AbstractJob job) {
		this.job = job;
	}

	public void setup(CloudgeneContext context) {

	}

	abstract public boolean run(WdlStep step, CloudgeneContext context);

	public int getMapProgress() {
		return 0;
	}

	public int getReduceProgress() {
		return 0;
	}

	public void updateProgress() {

	}

	public void kill() {

	}

	public List<Message> getLogMessages() {
		return logMessages;
	}

	public void setLogMessages(List<Message> logMessages) {
		this.logMessages = logMessages;
	}

	public void endTask(String message, int type) {
		Message status = getLogMessages().get(getLogMessages().size() - 1);
		status.setType(type);
		status.setMessage(message);
		/*
		 * if (type == LogMessage.ERROR) { println("[ERROR] " + message); } if
		 * (type == LogMessage.OK) { println("[OK] " + message); }
		 */
	}

	public void error() {
		Message status = getLogMessages().get(getLogMessages().size() - 1);
		status.setType(Message.ERROR);
	}

	public void message(String message, int type) {
		Message status = new Message(this, type, message);

		List<Message> logs = getLogMessages();
		if (logs == null) {
			logs = new Vector<Message>();
			setLogMessages(logs);
		}
		logs.add(status);

		/*
		 * if (type == LogMessage.ERROR) { println("[ERROR] " + message); } if
		 * (type == LogMessage.OK) { println("[OK] " + message); }
		 */

	}

	public void ok(String message) {
		// println("[OK] " + message);
		message(message, Message.OK);
	}

	public void error(String message) {
		// println("[ERROR] " + message);
		message(message, Message.ERROR);
	}

	public void warning(String message) {
		// println("[WARNING] " + message);
		message(message, Message.WARNING);
	}

	public void beginTask(String name) {
		Message status = new Message(this, Message.RUNNING, name);

		List<Message> logs = getLogMessages();
		if (logs == null) {
			logs = new Vector<Message>();
			setLogMessages(logs);
		}
		logs.add(status);
	}

	public void subTask(String name) {
		beginTask(name);
	}

	public void beginTask(String name, int totalWork) {
		beginTask(name);
	}

	public void endTask(int type) {
		Message status = getLogMessages().get(getLogMessages().size() - 1);
		status.setType(type);
	}

	public Message createLogMessage(String name, int type) {
		Message status = new Message(this, Message.RUNNING, name);
		List<Message> logs = getLogMessages();
		if (logs == null) {
			logs = new Vector<Message>();
			setLogMessages(logs);
		}
		logs.add(status);
		return status;
	}
	
	//dummy for beam serialization (setup --> property up!)
	public CloudgeneContext getup(){
		return null;
	}

}
