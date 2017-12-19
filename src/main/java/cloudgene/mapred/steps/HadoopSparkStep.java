package cloudgene.mapred.steps;

import java.util.List;
import java.util.Vector;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.jobs.Message;
import cloudgene.mapred.util.Technology;
import cloudgene.mapred.wdl.WdlStep;

public class HadoopSparkStep extends CloudgeneStep {

	@Override
	public boolean run(WdlStep step, CloudgeneContext context) {

		String pigPath = context.getSettings().getSparkPath();

		// params
		String paramsString = step.get("params");
		String[] params = new String[] {};
		if (paramsString != null) {
			params = paramsString.split(" ");
		}

		// spark script
		List<String> command = new Vector<String>();

		command.add(pigPath);
		command.add("--class");
		command.add(step.get("main_class"));
		command.add("--master");
		command.add("yarn");
		command.add(step.get("spark"));

		// params
		for (String tile : params) {
			command.add(tile.trim());
		}

		try {
			context.beginTask("Running Spark Script...");
			boolean successful = executeCommand(command, context);
			if (successful) {
				context.endTask("Execution successful.", Message.OK);
				return true;
			} else {
				context.endTask("Execution failed. Please have a look at the logfile for details.", Message.ERROR);
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	@Override
	public Technology[] getRequirements() {
		return new Technology[] { Technology.HADOOP_CLUSTER };
	}
}
