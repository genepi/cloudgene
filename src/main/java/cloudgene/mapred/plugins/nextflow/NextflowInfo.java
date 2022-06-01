package cloudgene.mapred.plugins.nextflow;

import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class NextflowInfo {

	private static NextflowInfo instance;
	
	private Map<String, List<NextflowProcess>> data;
	
	public static NextflowInfo getInstance() {
		if (instance == null) {
			instance = new NextflowInfo();
		}
		
		return instance;
	}
	
	private NextflowInfo() {
		data = new HashMap<String, List<NextflowProcess>>();
	}
	
	public void addEvent(String job, JSONObject event) {
		List<NextflowProcess> processes = data.get(job);
		if (processes == null) {
			processes = new Vector<NextflowProcess>();
			data.put(job, processes);
		}
		
		JSONObject trace = event.getJSONObject("trace");
		String processName = trace.getString("process");
		for (NextflowProcess process : processes) {
			if (process.getName().equals(processName)) {
				process.addTrace(trace);
				return;
			}
		}
		NextflowProcess process = new NextflowProcess(trace);
		processes.add(process);
	}
	
	public List<NextflowProcess> getProcesses(String job){
		List<NextflowProcess> processes = data.get(job);
		if (processes == null) {
			return new Vector<NextflowProcess>();
		}{
			return processes;
		}
	}
	
}
