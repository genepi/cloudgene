package cloudgene.mapred.tasks;

import cloudgene.mapred.jobs.TaskJob;

public interface ITask {

	public boolean execute();

	public int getProgress();

	public String getName();

	public String[] getParameters();

	public String[] getValues();
	
	public void setJob(TaskJob job);

}
