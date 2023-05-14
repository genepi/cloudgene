package cloudgene.mapred.plugins.nextflow;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import cloudgene.mapred.jobs.CloudgeneContext;

public class NextflowProcess {

	private String name;
	
	private CloudgeneContext context;

	private List<NextflowTask> tasks = new Vector<NextflowTask>();

	public NextflowProcess(CloudgeneContext context, Map<String, Object> trace) throws IOException {
		this.context = context;
		this.name = (String)trace.get("process");
		addTrace(trace);
	}

	public String getName() {
		return name;
	}

	public List<NextflowTask> getTasks() {
		return tasks;
	}

	public void addTrace(Map<String, Object> trace) throws IOException {
		int taskId = (Integer)trace.get("task_id");
		for (NextflowTask task : tasks) {
			if (task.getId() == taskId) {
				task.update(trace);
				return;
			}
		}
		NextflowTask task = new NextflowTask(context, trace);
		tasks.add(task);
	}

}
