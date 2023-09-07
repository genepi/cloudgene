package cloudgene.mapred.plugins.nextflow;

import java.io.File;
import java.util.List;
import java.util.Vector;

import cloudgene.mapred.util.BinaryFinder;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.util.command.Command;
import genepi.io.FileUtil;

public class NextflowBinary {

	private String binary = "";

	private String script;

	private String profile;

	private File nextflowConfigFile;

	private String work;

	private File paramsFile;

	private String weblog;

	private String trace;

	private String report;

	private String timeline;

	private String log;

	public static NextflowBinary build(Settings settings) {
		String binary = new BinaryFinder("nextflow").settings(settings, "nextflow", "home").env("NEXTFLOW_HOME")
				.envPath().path("/usr/local/bin").find();
		return new NextflowBinary(binary);
	}

	private NextflowBinary(String binary) {
		this.binary = binary;
	}

	public String getBinary() {
		return binary;
	}

	public boolean isInstalled() {
		if (binary != null) {
			String binary = getBinary();
			return (new File(binary)).exists();
		} else {
			return false;
		}
	}

	public String getVersion() {
		if (isInstalled()) {
			Command command = new Command(binary, "info");
			command.saveStdOut(FileUtil.path("info-version.txt"));
			command.setSilent(true);
			command.execute();
			String version = FileUtil.readFileAsString("info-version.txt");
			FileUtil.deleteFile("info-version.txt");
			return version;
		} else {
			return "Nextflow not installed.";
		}
	}

	public void setBinary(String binary) {
		this.binary = binary;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public void setNextflowConfigFile(File nextflowConfigFile) {
		this.nextflowConfigFile = nextflowConfigFile;
	}

	public void setWork(String work) {
		this.work = work;
	}

	public void setParamsFile(File paramsFile) {
		this.paramsFile = paramsFile;
	}

	public void setWeblog(String weblog) {
		this.weblog = weblog;
	}

	public void setTrace(String trace) {
		this.trace = trace;
	}

	public void setReport(String report) {
		this.report = report;
	}

	public void setTimeline(String timeline) {
		this.timeline = timeline;
	}

	public void setLog(String log) {
		this.log = log;
	}

	public List<String> buildCommand() {

		List<String> nextflow = new Vector<String>();
		nextflow.add("PATH=$PATH:/usr/local/bin");
		nextflow.add(getBinary());
		nextflow.add("run");
		nextflow.add(script);

		// set profile
		if (profile != null && !profile.isEmpty()) {
			nextflow.add("-profile");
			nextflow.add(profile);
		}

		if (nextflowConfigFile.exists()) {
			// set custom configuration
			nextflow.add("-c");
			nextflow.add(nextflowConfigFile.getAbsolutePath());
		}

		nextflow.add("-w");
		nextflow.add(work);

		nextflow.add("-params-file");
		nextflow.add(paramsFile.getAbsolutePath());

		nextflow.add("-ansi-log");
		nextflow.add("false");

		nextflow.add("-with-weblog");
		nextflow.add(weblog);

		// nextflowCommand.add("-log");
		// nextflowCommand.add(log);

		nextflow.add("-with-trace");
		nextflow.add(trace);

		nextflow.add("-with-report");
		nextflow.add(report);

		nextflow.add("-with-timeline");
		nextflow.add(timeline);

		List<String> command = new Vector<String>();
		command.add("/bin/bash");
		command.add("-c");
		command.add(join(nextflow));

		return command;

	}

	private String join(List<String> array) {
		String result = "";
		for (int i = 0; i < array.size(); i++) {
			if (i > 0) {
				result += " ";
			}
			result += array.get(i);
		}
		return result;
	}

}
