package cloudgene.mapred.tasks;

import cloudgene.mapred.jobs.TaskJob;

public abstract class AbstractTask implements ITask {

	private String name;

	protected TaskJob job;

	public void setJob(TaskJob job) {
		this.job = job;
	}

	public TaskJob getJob() {
		return job;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getProgress() {
		return 0;
	}

	public void writeOutput(String output) {
		job.writeOutput(output);
	}

}
