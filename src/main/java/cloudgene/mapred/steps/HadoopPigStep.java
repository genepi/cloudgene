package cloudgene.mapred.steps;

import java.util.List;
import java.util.Vector;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.jobs.Message;
import cloudgene.mapred.plugins.hadoop.HadoopPlugin;
import cloudgene.mapred.wdl.WdlStep;
import genepi.io.FileUtil;

public class HadoopPigStep extends CloudgeneStep {

	@Override
	public boolean run(WdlStep step, CloudgeneContext context) {

		String pigPath = context.getSettings().getPigPath();
		String pig = FileUtil.path(pigPath, "bin", "pig");

		// params
		String paramsString = step.get("params");
		String[] params = new String[] {};
		if (paramsString != null) {
			params = paramsString.split(" ");
		}

		// pig script
		List<String> command = new Vector<String>();

		command.add(pig);
		command.add("-f");
		command.add(step.get("pig"));

		// params
		for (String tile : params) {
			command.add(tile.trim());
		}

		try {
			context.beginTask("Running Pig Script...");
			boolean successful = executeCommand(command, context);
			if (successful) {
				context.endTask("Execution successful.", Message.OK);
				return true;
			} else {
				context.endTask("Execution failed. Please contact the server administrators for help if you believe this job should have completed successfully.",
						Message.ERROR);
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}
	
	@Override
	public String[] getRequirements() {
		return new String[] { HadoopPlugin.ID };
	}

}
