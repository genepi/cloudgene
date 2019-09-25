package cloudgene.mapred.plugins.nextflow;

import net.sf.json.JSONObject;

public class NextflowTask {

	private int id;

	private JSONObject trace;

	public NextflowTask(JSONObject trace) {
		id = trace.getInt("task_id");
		this.trace = trace;
	}

	public void update(JSONObject trace) {
		this.trace = trace;
	}

	public int getId() {
		return id;
	}
	
	public JSONObject getTrace() {
		return trace;
	}

}
