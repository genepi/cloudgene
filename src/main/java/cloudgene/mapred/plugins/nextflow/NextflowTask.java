package cloudgene.mapred.plugins.nextflow;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.workspace.IWorkspace;
import cloudgene.mapred.plugins.nextflow.report.Report;
import cloudgene.mapred.plugins.nextflow.report.ReportEvent;
import cloudgene.mapred.plugins.nextflow.report.ReportEventExecutor;
import genepi.io.FileUtil;

public class NextflowTask {

	private int id;

	private Map<String, Object> trace;

	private String logText = null;

	private CloudgeneContext context;

	private static final Logger log = LoggerFactory.getLogger(NextflowTask.class);
	
	public NextflowTask(CloudgeneContext context, Map<String, Object> trace) {
		id = (Integer) trace.get("task_id");
		this.trace = trace;
		this.context = context;
	}

	public void update(Map<String, Object> trace) throws IOException {
		this.trace = trace;

		// if task is completed or failed check if a cloudgene.log is in workdir and
		// load its content

		// TODO: check if CHACHED os also needed!
		String status = (String) trace.get("status");
		if (!status.equals("COMPLETED") && !status.equals("FAILED")) {
			return;
		}

		String workDir = (String) trace.get("workdir");
		String reportFilename = FileUtil.path(workDir, Report.DEFAULT_FILENAME);
		IWorkspace workspace = context.getJob().getWorkspace();
		if (!workspace.exists(reportFilename)) {
			return;
		}

		context.log("Load report file from '" + reportFilename + "'");
		InputStream stream = workspace.download(reportFilename);
		try {
			parseReport(reportFilename);
		} catch (Exception e) {
			log.error("[Job {}] Invalid report file.", e);
			logText = "Invalid report file: \n" + FileUtil.readFileAsString(stream);
		}

	}

	private void parseReport(String reportFilename) throws IOException {
		InputStream stream = context.getWorkspace().download(reportFilename);
		Report report = new Report(stream);
		for (ReportEvent event : report.getEvents()) {
			ReportEventExecutor.execute(event, context);
		}
	}

	public int getId() {
		return id;
	}

	public Map<String, Object> getTrace() {
		return trace;
	}

	public String getLogText() {
		return logText;
	}

}
