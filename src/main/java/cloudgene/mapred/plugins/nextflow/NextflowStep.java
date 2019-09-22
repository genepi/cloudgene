package cloudgene.mapred.plugins.nextflow;

import java.io.File;
import java.util.List;
import java.util.Vector;

import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.jobs.Message;
import cloudgene.mapred.wdl.WdlStep;
import genepi.hadoop.common.WorkflowContext;
import genepi.io.FileUtil;

public class NextflowStep extends CloudgeneStep {

	private CloudgeneContext context;

	private boolean running = false;

	@Override
	public boolean run(WdlStep step, CloudgeneContext context) {

		this.context = context;

		String script = step.get("script");

		if (script == null) {
			// no script set. try to find a main.nf. IS the default convention of nf-core.
			script = "main.nf";
		}

		String scriptPath = FileUtil.path(context.getWorkingDirectory(), script);
		if (!new File(scriptPath).exists()) {
			context.error(
					"Nextflow script '" + scriptPath + "' not found. Please use 'script' to define your nf file.");
		}

		NextflowBinary nextflow = NextflowBinary.build(context.getSettings());

		List<String> command = new Vector<String>();
		command.add(nextflow.getBinary());
		command.add("run");
		command.add(script);

		AbstractJob job = context.getJob();
		String appFolder = context.getSettings().getApplicationRepository().getConfigDirectory(job.getApplicationId());

		String profile = "";
		String nextflowProfile = FileUtil.path(appFolder, "nextflow.profile");
		if (new File(nextflowProfile).exists()) {
			profile = FileUtil.readFileAsString(nextflowProfile);
		}

		// set profile
		if (!profile.isEmpty()) {
			command.add("-profile");
			command.add(profile);
		}

		String nextflowConfig = FileUtil.path(appFolder, "nextflow.config");
		File nextflowConfigFile = new File(nextflowConfig);
		if (nextflowConfigFile.exists()) {
			// set custom configuration
			command.add("-c");
			command.add(nextflowConfigFile.getAbsolutePath());
		}

		String work = "";
		String nextflowWork = FileUtil.path(appFolder, "nextflow.work");
		if (new File(nextflowWork).exists()) {
			work = FileUtil.readFileAsString(nextflowWork);
		}

		// use workdir if set in settings
		if (!work.trim().isEmpty()) {
			command.add("-w");
			command.add(work);
		} else {
			String workDir = FileUtil.path(context.getLocalTemp(), "nextflow");
			FileUtil.createDirectory(workDir);
			command.add("-w");
			command.add(workDir);
		}

		// used to defined hard coded params
		for (String key : step.keySet()) {
			if (key.startsWith("params.")) {
				String param = key.replace("params.", "");
				String value = step.get(key);
				command.add("--" + param + "=" + value);
			}
		}

		// add all inputs
		for (String param : context.getInputs()) {
			String value = context.getInput(param);
			command.add("--" + param + "=" + value);

		}

		// add all outputs
		for (String param : context.getOutputs()) {
			String value = context.getOutput(param);
			command.add("--" + param + "=" + value);

		}

		command.add("-ansi-log");
		command.add("false");

		command.add("-with-weblog");
		command.add("http://localhost:8082/api/v2/collect/" + context.getJobId());

		StringBuilder output = new StringBuilder();

		try {
			context.beginTask("Running Nextflow pipeline...");
			running = true;
			boolean successful = executeCommand(command, context, output);
			running = false;
			if (successful) {
				context.endTask(getNextflowInfo(), Message.OK);

				return true;
			} else {

				String text = "Pipeline execution failed.<br><br><pre style=\"font-size: 12px\">" + output + "</pre>";
				context.endTask(text, Message.ERROR);

				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	private String getNextflowInfo() {
		String job = context.getJobId();

		List<NextflowProcess> processes = NextflowInfo.getInstance().getProcesses(job);
		String text = "";
		for (NextflowProcess process : processes) {
			text += "<b>" + process.getName() + "</b><br>";
			text += "<ul>";
			for (NextflowTask task : process.getTasks()) {
				text += "<li>" + task.getTrace().getString("name") + " (" + task.getTrace().getString("status") + ")"
						+ "</li>";
			}
			text += "</ul>";
		}

		if (text.isEmpty()) {
			return "Preparing execution....";
		}

		return text;
	}

	@Override
	public void updateProgress() {
		if (running) {
			String text = getNextflowInfo();
			context.updateTask(text, WorkflowContext.RUNNING);
		}
	}

	@Override
	public String[] getRequirements() {
		return new String[] { NextflowPlugin.ID };
	}

}
