package cloudgene.mapred.plugins.nextflow;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.jobs.Message;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.wdl.WdlStep;
import genepi.io.FileUtil;

public class NextflowStep extends CloudgeneStep {

	private CloudgeneContext context;

	private boolean running = false;

	private Map<String, Message> tasks = new HashMap<String, Message>();

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
		command.add("http://localhost:8082/api/v2/collect/" + makeSecretJobId(context.getJobId()));

		StringBuilder output = new StringBuilder();

		try {
			// context.beginTask("Running Nextflow pipeline...");
			running = true;
			boolean successful = executeCommand(command, context, output);
			running = false;
			if (successful) {
				// context.endTask(getNextflowInfo(), Message.OK);
				updateNextflowInfo();
				return true;
			} else {
				updateNextflowInfo();
				context.beginTask("Running Nextflow pipeline...");
				String text = "Pipeline execution failed.<br><br><pre style=\"font-size: 12px\">" + output + "</pre>";
				context.endTask(text, Message.ERROR);

				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	private void updateNextflowInfo() {
		String job = makeSecretJobId(context.getJobId());

		List<NextflowProcess> processes = NextflowInfo.getInstance().getProcesses(job);

		for (NextflowProcess process : processes) {

			Message stepTask = tasks.get(process.getName());
			if (stepTask == null) {
				stepTask = context.createTask("<b>" + process.getName() + "</b>");
				tasks.put(process.getName(), stepTask);
			}

			String text = "<b>" + process.getName() + "</b>";
			boolean running = false;
			boolean ok = true;
			for (NextflowTask task : process.getTasks()) {
				if (task.getTrace().getString("status").equals("RUNNING")) {
					running = true;
				}
				if (!task.getTrace().getString("status").equals("COMPLETED")) {
					ok = false;

				}
				text += "<br><small>";

				text += task.getTrace().getString("name");
				if (task.getTrace().getString("status").equals("RUNNING")) {
					text += "...";
				}
				if (task.getTrace().getString("status").equals("COMPLETED")) {
					text += "&nbsp;<i class=\"fas fa-check text-success\"></i>";
				}
				text+= "</small>";
			}

			if (running) {
				stepTask.setType(Message.RUNNING);
			} else {
				if (ok) {
					stepTask.setType(Message.OK);
				} else {
					stepTask.setType(Message.ERROR);
				}
			}
			stepTask.setMessage(text);
			// TODO: set status
		}

	}

	@Override
	public void updateProgress() {
		if (running) {
			updateNextflowInfo();
		}
	}

	@Override
	public String[] getRequirements() {
		return new String[] { NextflowPlugin.ID };
	}

	public String makeSecretJobId(String job) {
		return HashUtil.getMD5(job);
	}
	
}
