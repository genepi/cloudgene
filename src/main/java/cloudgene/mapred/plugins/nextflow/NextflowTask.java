package cloudgene.mapred.plugins.nextflow;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.sdk.internal.IExternalWorkspace;
import genepi.io.FileUtil;
import genepi.io.text.LineReader;

public class NextflowTask {

	private static final String CLOUDGENE_LOG = "cloudgene.log";

	private int id;

	private Map<String, Object> trace;

	private String log = null;

	private CloudgeneContext context;

	public NextflowTask(CloudgeneContext context, Map<String, Object> trace) {
		id = (Integer) trace.get("task_id");
		this.trace = trace;
		this.context = context;
	}

	public void update(Map<String, Object> trace) throws IOException {
		this.trace = trace;

		// if task is completed or failed check if a cloudgene.log is in workdir and
		// load its content

		String status = (String) trace.get("status");
		if (status.equals("COMPLETED") || status.equals("FAILED")) {
			String workDir = (String) trace.get("workdir");
			String logFilename = FileUtil.path(workDir, CLOUDGENE_LOG);
			IExternalWorkspace workspace = context.getExternalWorkspace();
			InputStream stream = workspace.download(logFilename);
			log = FileUtil.readFileAsString(stream);
			parseFile(logFilename);
		}

	}

	private void parseFile(String logFilename) throws IOException {

		LineReader reader = new LineReader(new DataInputStream(context.getExternalWorkspace().download(logFilename)));
		while (reader.next()) {
			String line = reader.get();
			if (line.startsWith("[INC]")) {
				String[] tiles = line.split(" ", 3);
				String name = tiles[1];
				int value = Integer.parseInt(tiles[2]);
				context.incCounter(name, value);
			}
			if (line.startsWith("[SUBMIT]")) {
				String[] tiles = line.split(" ", 2);
				String name = tiles[1];
				context.submitCounter(name);
			}
		}
		reader.close();
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
