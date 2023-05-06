package cloudgene.mapred.plugins.nextflow;

import java.io.File;

import genepi.io.FileUtil;
import net.sf.json.JSONObject;

public class NextflowTask {

	private int id;

	private JSONObject trace;

	private String log = null;

	public NextflowTask(JSONObject trace) {
		id = trace.getInt("task_id");
		this.trace = trace;
	}

	public void update(JSONObject trace) {
		this.trace = trace;

		// if task is completed or failed check if a cloudgene.log is in workdir and
		// load its content

		String status = trace.getString("status");
		if (status.equals("COMPLETED") || status.equals("FAILED")) {
			String workDir = trace.getString("workdir");
			String logFilename = FileUtil.path(workDir, "cloudgene.log");

			// TODO: implement s3 support. How to handle other cloud providers?

			if (new File(logFilename).exists()) {
				log = FileUtil.readFileAsString(logFilename);
			}
		}

	}

	public int getId() {
		return id;
	}

	public JSONObject getTrace() {
		return trace;
	}

	public String getLog() {
		return log;
	}

}
