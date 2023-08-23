package cloudgene.mapred.plugins.nextflow;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.jobs.Message;
import cloudgene.mapred.jobs.workspace.WorkspaceFactory;
import cloudgene.mapred.jobs.workspace.IWorkspace;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.wdl.WdlStep;
import genepi.io.FileUtil;
import groovy.json.JsonOutput;
import jakarta.inject.Inject;

public class NextflowStep extends CloudgeneStep {

	private CloudgeneContext context;

	private Map<String, Message> messages = new HashMap<String, Message>();
	
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

		List<String> nextflowCommand = new Vector<String>();
		nextflowCommand.add("PATH=$PATH:/usr/local/bin");
		nextflowCommand.add(nextflow.getBinary());
		nextflowCommand.add("run");
		nextflowCommand.add(script);

		AbstractJob job = context.getJob();
		String appFolder = context.getSettings().getApplicationRepository().getConfigDirectory(job.getApplicationId());

		String profile = "";
		String nextflowProfile = FileUtil.path(appFolder, "nextflow.profile");
		if (new File(nextflowProfile).exists()) {
			profile = FileUtil.readFileAsString(nextflowProfile);
		}

		// set profile
		if (!profile.isEmpty()) {
			nextflowCommand.add("-profile");
			nextflowCommand.add(profile);
		}

		String nextflowConfig = FileUtil.path(appFolder, "nextflow.config");
		File nextflowConfigFile = new File(nextflowConfig);
		if (nextflowConfigFile.exists()) {
			// set custom configuration
			nextflowCommand.add("-c");
			nextflowCommand.add(nextflowConfigFile.getAbsolutePath());
		}

		String work = "";
		String nextflowWork = FileUtil.path(appFolder, "nextflow.work");
		if (new File(nextflowWork).exists()) {
			work = FileUtil.readFileAsString(nextflowWork);
		}

		// use workdir if set in settings
		if (!work.trim().isEmpty()) {
			nextflowCommand.add("-w");
			nextflowCommand.add(work);
		} else {
			IWorkspace workspace = job.getWorkspace();
			String workDir = workspace.createTempFolder("nextflow");
			FileUtil.createDirectory(workDir);
			nextflowCommand.add("-w");
			nextflowCommand.add(workDir);
		}

		Map<String, Object> params = new HashMap<String, Object>();

		// used to defined hard coded params
		for (String key : step.keySet()) {
			if (key.startsWith("params.")) {
				String param = key.replace("params.", "");
				String value = step.get(key);
				params.put(param, value);
			}
		}

		// add all inputs
		for (String param : context.getInputs()) {
			String value = context.getInput(param);
			// resolve app links: use all properties as input parameters
			if (value.startsWith("apps@")) {
				Map<String, Object> linkedApp = (Map<String, Object>) context.getData(param);
				params.put(param, linkedApp);
			} else {
				params.put(param, value);
			}

		}

		// add all outputs
		for (String param : context.getOutputs()) {
			String value = context.getOutput(param);
			params.put(param, value);
		}

		String paramsJsonFilename = FileUtil.path(context.getLocalOutput(), "params.json");
		File paramsFile = new File(paramsJsonFilename);

		try {
			writeParamsJson(params, paramsFile);
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}

		nextflowCommand.add("-params-file");
		nextflowCommand.add(paramsFile.getAbsolutePath());

		nextflowCommand.add("-ansi-log");
		nextflowCommand.add("false");

		nextflowCommand.add("-with-weblog");
		nextflowCommand.add(context.getSettings().getServerUrl() + context.getSettings().getUrlPrefix()
				+ "/api/v2/collect/" + makeSecretJobId(context.getJobId()));

		StringBuilder output = new StringBuilder();

		List<String> command = new Vector<String>();
		command.add("/bin/bash");
		command.add("-c");
		command.add(join(nextflowCommand));

		NextflowCollector.getInstance().addContext(makeSecretJobId(context.getJobId()), context);

		try {
			// context.beginTask("Running Nextflow pipeline...");
			boolean successful = executeCommand(command, context, output);
			if (successful) {
				updateProgress();
				return true;
			} else {

				// set all running processes to failed
				List<NextflowProcess> processes = NextflowCollector.getInstance()
						.getProcesses(makeSecretJobId(context.getJobId()));
				for (NextflowProcess process : processes) {
					for (NextflowTask task : process.getTasks()) {
						String status = (String) task.getTrace().get("status");

						if (status.equals("RUNNING") || status.equals("SUBMITTED")) {
							task.getTrace().put("status", "KILLED");
						}
					}
				}
				updateProgress();

				// Write nextflow output into step
				/*
				 * context.beginTask("Running Nextflow pipeline..."); String text = "";
				 * 
				 * if (killed) { text = output + "\n\n\n" +
				 * makeRed("Pipeline execution canceled."); } else { text = output + "\n\n\n" +
				 * makeRed("Pipeline execution failed."); } context.endTask(text,
				 * Message.ERROR_ANSI);
				 */
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	@Override
	public void updateProgress() {
		String job = makeSecretJobId(context.getJobId());

		List<NextflowProcess> processes = NextflowCollector.getInstance().getProcesses(job);

		for (NextflowProcess process : processes) {

			Message message = messages.get(process.getName());
			if (message == null) {
				message = context.createTask("<b>" + process.getName() + "</b>");
				messages.put(process.getName(), message);
			}

			String text = "<b>" + process.getName() + "</b>";
			boolean running = false;
			boolean ok = true;
			for (NextflowTask task : process.getTasks()) {

				String status = (String) task.getTrace().get("status");

				if (status.equals("RUNNING") || status.equals("SUBMITTED")) {
					running = true;
				}
				if (!status.equals("COMPLETED")) {
					ok = false;

				}
				text += "<br><small>";

				text += (String) task.getTrace().get("name");
				if (status.equals("RUNNING")) {
					text += "...";
				}
				if (status.equals("COMPLETED")) {
					text += "&nbsp;<i class=\"fas fa-check text-success\"></i>";
				}
				if (status.equals("KILLED") || status.equals("FAILED")) {
					text += "&nbsp;<i class=\"fas fa-times text-danger\"></i>";
				}

				if (task.getLog() != null) {
					text += "<br>" + task.getLog();
				}

				text += "</small>";
			}
			message.setMessage(text);

			if (running) {
				message.setType(Message.RUNNING);
			} else {
				if (ok) {
					message.setType(Message.OK);
				} else {
					message.setType(Message.ERROR);
				}
			}

		}
	}

	private String join(List<String> array) {
		String result = "";
		for (int i = 0; i < array.size(); i++) {
			if (i > 0) {
				result += " \\\n";
			}
			result += array.get(i);
		}
		return result;
	}

	@Override
	public String[] getRequirements() {
		return new String[] { NextflowPlugin.ID };
	}

	public String makeSecretJobId(String job) {
		return HashUtil.getSha256(job);
	}

	private String makeRed(String text) {
		return ((char) 27 + "[31m" + text + (char) 27 + "[0m");
	}

	protected void writeParamsJson(Map<String, Object> params, File paramsFile) throws IOException {

		BufferedWriter writer = new BufferedWriter(new FileWriter(paramsFile));
		writer.write(JsonOutput.toJson(params));
		writer.close();

	}

}
