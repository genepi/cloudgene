package cloudgene.mapred.plugins.nextflow;

import java.util.List;
import java.util.Vector;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.jobs.Message;
import cloudgene.mapred.wdl.WdlStep;
import genepi.hadoop.common.WorkflowContext;
import net.sf.json.JSONObject;

public class NextflowStep extends CloudgeneStep {

	private CloudgeneContext context;
	
	@Override
	public boolean run(WdlStep step, CloudgeneContext context) {

		this.context = context;
		
		String script = step.get("script");

		if (script == null) {
			context.error("No 'script' parameter found. Please use this parameter to define your nf file.");
		}

		NextflowBinary nextflow = NextflowBinary.build(context.getSettings());

		List<String> command = new Vector<String>();
		command.add(nextflow.getBinary());
		command.add("run");
		command.add(script);
		for (String key : step.keySet()) {
			if (key.startsWith("params.")) {
				String param = key.replace("params.", "");
				String value = step.get(key);
				command.add("--" + param + "=" + value);
			}
		}

		command.add("-with-docker");
		command.add("-with-weblog");
		command.add("http://localhost:8082/api/v2/collect/" + context.getJobId());

		StringBuilder output = null;


		try {
			context.beginTask("Running Nextflow pipeline...");
			boolean successful = executeCommand(command, context, output);
			if (successful) {
						context.endTask(getNextflowInfo(), Message.OK);
				
				return true;
			} else {

					context.endTask(getNextflowInfo(), Message.ERROR);
				
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}
	
	private String getNextflowInfo() {
		String job = context.getJobId();
		List<JSONObject> events = NextflowInfo.getInstance().getEvents(job);
		String text = "";
		for (JSONObject event: events) {
			JSONObject trace = event.getJSONObject("trace");
			text+=  "[" + trace.getString("hash") + "] "+ trace.getString("name") + ": " + trace.getString("status") + "<br>";
		}
		return text;
	}
	
	@Override
	public void updateProgress() {
		super.updateProgress();
			String text = getNextflowInfo();
			context.updateTask(text, WorkflowContext.RUNNING);
		
	}

	@Override
	public String[] getRequirements() {
		return new String[] { NextflowPlugin.ID };
	}

}
