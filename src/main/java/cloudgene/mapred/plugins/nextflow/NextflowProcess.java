package cloudgene.mapred.plugins.nextflow;

import net.sf.json.JSONObject;

import java.util.List;
import java.util.Vector;

public class NextflowProcess {

	private String name;

	private List<NextflowTask> tasks = new Vector<NextflowTask>();

	public NextflowProcess(JSONObject trace) {
		this.name = trace.getString("process");
		addTrace(trace);
	}

	public String getName() {
		return name;
	}

	public List<NextflowTask> getTasks() {
		return tasks;
	}

	public void addTrace(JSONObject trace) {
		int taskId = trace.getInt("task_id");
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
