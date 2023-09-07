package cloudgene.mapred.plugins.nextflow;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.jobs.Message;
import cloudgene.mapred.jobs.workspace.IWorkspace;
import cloudgene.mapred.wdl.WdlStep;
import genepi.io.FileUtil;
import groovy.json.JsonOutput;

public class NextflowStep extends CloudgeneStep {

	private static final String PROPERTY_PROCESS_CONFIG = "processes";

	private CloudgeneContext context;

	private Map<String, Message> messages = new HashMap<String, Message>();

	private Map<String, NextflowProcessConfig> configs = new HashMap<String, NextflowProcessConfig>();

	private NextflowCollector collector = NextflowCollector.getInstance();

	private static final Logger log = LoggerFactory.getLogger(NextflowStep.class);

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
		nextflow.setScript(script);

		AbstractJob job = context.getJob();
		String appFolder = context.getSettings().getApplicationRepository().getConfigDirectory(job.getApplicationId());

		// set profile
		String profile = "";
		String nextflowProfile = FileUtil.path(appFolder, "nextflow.profile");
		if (new File(nextflowProfile).exists()) {
			profile = FileUtil.readFileAsString(nextflowProfile);
		}
		nextflow.setProfile(profile);

		// set custom configuration
		String nextflowConfig = FileUtil.path(appFolder, "nextflow.config");
		File nextflowConfigFile = new File(nextflowConfig);
		nextflow.setNextflowConfigFile(nextflowConfigFile);

		// set work directory
		String work = "";
		String nextflowWork = FileUtil.path(appFolder, "nextflow.work");
		if (new File(nextflowWork).exists()) {
			work = FileUtil.readFileAsString(nextflowWork);
		}

		IWorkspace workspace = job.getWorkspace();

		// use workdir if set in settings
		if (!work.trim().isEmpty()) {
			nextflow.setWork(work);
		} else {
			String workDir = workspace.createTempFolder("nextflow");
			nextflow.setWork(workDir);
		}

		//params json file		
		String paramsJsonFilename = FileUtil.path(context.getLocalOutput(), "params.json");
		File paramsFile = new File(paramsJsonFilename);
		try {
			Map<String, Object> params = createParamsMap(step);
			// TODO: workspace?
			writeParamsJson(params, paramsFile);
		} catch (IOException e) {
			log.error("[Job {}] Writing params.json file failed.", context.getJobId(), e);
			return false;
		}
		nextflow.setParamsFile(paramsFile);
		
		//register job in webcollector and set created url
		String collectorUrl = collector.addContext(context);
		nextflow.setWeblog(collectorUrl);

		//log files and reports
		nextflow.setTrace(workspace.createLogFile("trace.csv"));
		nextflow.setReport(workspace.createLogFile("report.html"));
		nextflow.setTimeline(workspace.createLogFile("timeline.html"));
		nextflow.setLog(workspace.createLogFile("nextflow.log"));

		try {

			StringBuilder output = new StringBuilder();
			boolean successful = executeCommand(nextflow.buildCommand(), context, output);

			if (!successful) {

				// set all running processes to failed
				List<NextflowProcess> processes = collector.getProcesses(context);
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

			}

			updateProgress();

			collector.cleanProcesses(context);

			return successful;

		} catch (Exception e) {
			log.error("[Job {}] Running nextflow script failed.", context.getJobId(), e);
			return false;
		}

	}

	@Override
	public void updateProgress() {

		List<NextflowProcess> processes = collector.getProcesses(context);

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

	@Override
	public String[] getRequirements() {
		return new String[] { NextflowPlugin.ID };
	}
	
	private Map<String, Object> createParamsMap(WdlStep step) {
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

		return params;

	}

	protected void writeParamsJson(Map<String, Object> params, File paramsFile) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(paramsFile));
		writer.write(JsonOutput.toJson(params));
		writer.close();
	}

}
