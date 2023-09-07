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
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.jobs.Message;
import cloudgene.mapred.jobs.workspace.IWorkspace;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.wdl.WdlStep;
import genepi.io.FileUtil;
import groovy.json.JsonOutput;

public class NextflowStep extends CloudgeneStep {

	private static final String PROPERTY_PROCESS_CONFIG = "processes";

	private CloudgeneContext context;

	private Map<String, Message> messages = new HashMap<String, Message>();

	private Map<String, NextflowProcessConfig> configs = new HashMap<String, NextflowProcessConfig>();

	@Override
	public boolean run(WdlStep step, CloudgeneContext context) {

		this.context = context;

		String script = step.getString("script");

		if (script == null) {
			// no script set. try to find a main.nf. IS the default convention of nf-core.
			script = "main.nf";
		}

		String scriptPath = FileUtil.path(context.getWorkingDirectory(), script);
		if (!new File(scriptPath).exists()) {
			context.error(
					"Nextflow script '" + scriptPath + "' not found. Please use 'script' to define your nf file.");
		}

		// load process styling
		loadProcessConfigs(step.get(PROPERTY_PROCESS_CONFIG));

		NextflowBinary nextflow = NextflowBinary.build(context.getSettings());
		// TODO: move to bextflow binary. see nftest implementation
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
			nextflowCommand.add("-w");
			nextflowCommand.add(workDir);
		}

		Map<String, Object> params = new HashMap<String, Object>();

		// used to defined hard coded params
		for (String key : step.keySet()) {
			if (key.startsWith("params.")) {
				String param = key.replace("params.", "");
				Object value = step.get(key);
				params.put(param, value);
			}
		}
		if (step.get("params") != null) {
			Map<String, Object> paramsMap = (Map<String, Object>) step.get("params");
			params.putAll(paramsMap);
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

		IWorkspace workspace = job.getWorkspace();

		nextflowCommand.add("-params-file");
		nextflowCommand.add(paramsFile.getAbsolutePath());

		nextflowCommand.add("-ansi-log");
		nextflowCommand.add("false");

		nextflowCommand.add("-with-weblog");
		nextflowCommand.add(context.getSettings().getServerUrl() + context.getSettings().getUrlPrefix()
				+ "/api/v2/collect/" + makeSecretJobId(context.getJobId()));

		// nextflowCommand.add("-log");
		// nextflowCommand.add(workspace.createLogFile("nextflow.log"));

		nextflowCommand.add("-with-trace");
		nextflowCommand.add(workspace.createLogFile("trace.csv"));

		nextflowCommand.add("-with-report");
		nextflowCommand.add(workspace.createLogFile("report.html"));

		nextflowCommand.add("-with-timeline");
		nextflowCommand.add(workspace.createLogFile("timeline.html"));

		StringBuilder output = new StringBuilder();

		List<String> command = new Vector<String>();
		command.add("/bin/bash");
		command.add("-c");
		command.add(join(nextflowCommand));

		NextflowCollector.getInstance().addContext(makeSecretJobId(context.getJobId()), context);

		try {

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

				if (killed) {
					context.error("Pipeline execution canceled.");
				}

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

			NextflowProcessConfig config = getNextflowProcessConfig(process);

			Message message = messages.get(process.getName());
			if (message == null) {
				message = context.createTask("<b>" + process.getName() + "</b>");
				messages.put(process.getName(), message);
			}

			NextflowProcessRenderer.render(config, process, message);

		}

	}

	private void loadProcessConfigs(Object map) {
		if (map != null) {
			List<Map<String, Object>> processConfigs = (List<Map<String, Object>>) map;
			for (Map<String, Object> processConfig : processConfigs) {
				String process = processConfig.get("process").toString();
				NextflowProcessConfig config = new NextflowProcessConfig();
				if (processConfig.get("view") != null) {
					config.setView(processConfig.get("view").toString());
				}
				configs.put(process, config);
			}
		}
	}

	private NextflowProcessConfig getNextflowProcessConfig(NextflowProcess process) {
		NextflowProcessConfig config = configs.get(process.getName());
		return config != null ? config : new NextflowProcessConfig();
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
