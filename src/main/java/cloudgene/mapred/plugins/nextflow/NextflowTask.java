package cloudgene.mapred.plugins.nextflow;

import java.io.File;
import java.util.Map;

import genepi.io.FileUtil;

public class NextflowTask {

	private int id;

	private Map<String, Object> trace;

	private String log = null;

	public NextflowTask(Map<String, Object> trace) {
		id = (Integer) trace.get("task_id");
		this.trace = trace;
	}

	public void update(Map<String, Object> trace) {
		this.trace = trace;

		// if task is completed or failed check if a cloudgene.log is in workdir and
		// load its content

		String status = (String) trace.get("status");
		if (status.equals("COMPLETED") || status.equals("FAILED")) {
			String workDir = (String) trace.get("workdir");
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

	public Map<String, Object> getTrace() {
		return trace;
	}

	public String getLog() {
		return log;
	}

}
