package cloudgene.mapred.jobs;

import java.io.File;
import java.util.List;

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

	// dummy for beam serialization (setup --> property up!)
	public CloudgeneContext getup() {
		return null;
	}

}
