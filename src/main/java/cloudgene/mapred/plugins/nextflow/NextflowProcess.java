package cloudgene.mapred.plugins.nextflow;

import java.util.List;
import java.util.Map;
import java.util.Vector;

public class NextflowProcess {

	private String name;

	private List<NextflowTask> tasks = new Vector<NextflowTask>();

	public NextflowProcess(Map<String, Object> trace) {
		this.name = (String)trace.get("process");
		addTrace(trace);
	}

	public String getName() {
		return name;
	}

	public List<NextflowTask> getTasks() {
		return tasks;
	}

	public void addTrace(Map<String, Object> trace) {
		int taskId = (Integer)trace.get("task_id");
		for (NextflowTask task : tasks) {
			if (task.getId() == taskId) {
				task.update(trace);
				return;
			}
		}
		NextflowTask task = new NextflowTask(trace);
		tasks.add(task);
	}

}
