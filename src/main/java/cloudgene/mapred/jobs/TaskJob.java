package cloudgene.mapred.jobs;

import genepi.io.FileUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import cloudgene.mapred.tasks.ITask;
import cloudgene.mapred.util.Settings;

public class TaskJob extends AbstractJob {

	private ITask task;

	private List<ITask> tasks;

	public TaskJob() {

	}

	public TaskJob(ITask task) {

		this.task = task;
		task.setJob(this);

		tasks = new Vector<ITask>();
		tasks.add(task);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
		setId(task.getName() + "-" + sdf.format(new Date()));

		List<CloudgeneParameter> inputParams = new Vector<CloudgeneParameter>();

		for (int i = 0; i < task.getParameters().length; i++) {

			String param = task.getParameters()[i];
			String value = task.getValues()[i];

			CloudgeneParameter parameter = new CloudgeneParameter();
			parameter.setName("input_" + i);
			parameter.setDescription(param);
			parameter.setValue(value);

			inputParams.add(parameter);

		}

		setInputParams(inputParams);
	}

	@Override
	public boolean before() {

		return true;

	}

	@Override
	public boolean setup() {
		return true;
	}

	@Override
	public boolean execute() {

		String localWorkspace = getLocalWorkspace();

		String localOutputDirectory = FileUtil.path(localWorkspace, "output",
				getId());

		FileUtil.createDirectory(localOutputDirectory);

		// setCurrentStep("Importing...");

		return task.execute();

	}

	@Override
	public boolean executeSetup() {
		return true;
	}

	@Override
	public boolean after() {

		return true;

	}

	@Override
	public boolean cleanUp() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFailure() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getMap() {
		if (task != null) {
			return task.getProgress();
		} else {
			return -1;
		}
	}

	@Override
	public int getReduce() {
		return -1;
	}

	public void setTask(ITask task) {
		this.task = task;
	}

	public ITask getTask() {
		return task;
	}

	@Override
	public int getType() {
		return AbstractJob.TYPE_TASK;
	}

}
