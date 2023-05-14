package cloudgene.mapred.plugins.nextflow;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import cloudgene.mapred.jobs.CloudgeneContext;

public class NextflowCollector {
	
	private static NextflowCollector instance;

	private Map<String, List<NextflowProcess>> data;

	private Map<String, CloudgeneContext> contexts;

	public static NextflowCollector getInstance() {
		if (instance == null) {
			instance = new NextflowCollector();
		}

		return instance;
	}

	private NextflowCollector() {
		contexts = new HashMap<String, CloudgeneContext>();
		data = new HashMap<String, List<NextflowProcess>>();
	}

	public void addContext(String job, CloudgeneContext context) {
		contexts.put(job, context);
	}

	public void addEvent(String job, Map<String, Object> event) throws IOException {
		
		CloudgeneContext context = contexts.get(job);
		
		if (context == null) {
			System.out.println("Warning! No context found for job " + job);
			return; 
		}
		
		List<NextflowProcess> processes = data.get(job);
		if (processes == null) {
			processes = new Vector<NextflowProcess>();
			data.put(job, processes);
		}

		if (!event.containsKey("trace")) {
			return;
		}

		Map<String, Object> trace = (Map<String, Object>) event.get("trace");
		if (!trace.containsKey("process")) {
			return;
		}

		String processName = trace.get("process").toString();
		for (NextflowProcess process : processes) {
			if (process.getName().equals(processName)) {
				process.addTrace(trace);
				return;
			}
		}
		NextflowProcess process = new NextflowProcess(context, trace);
		processes.add(process);
	}

	public List<NextflowProcess> getProcesses(String job) {
		List<NextflowProcess> processes = data.get(job);
		if (processes == null) {
			return new Vector<NextflowProcess>();
		}
		{
			return processes;
		}
	}

}
