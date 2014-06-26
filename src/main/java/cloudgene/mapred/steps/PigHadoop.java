package cloudgene.mapred.steps;

import genepi.io.FileUtil;

import java.util.List;
import java.util.Vector;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.Message;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlStep;

public class PigHadoop extends Hadoop {

	@Override
	public boolean run(WdlStep step, CloudgeneContext context) {

		String pigPath = Settings.getInstance().getPigPath();
		String pig = FileUtil.path(pigPath, "bin", "pig");

		// params
		String paramsString = step.getParams();
		String[] params = paramsString.split(" ");

		// pig script
		List<String> command = new Vector<String>();

		command.add(pig);
		command.add("-f");
		command.add(step.getPig());

		// params
		for (String tile : params) {
			command.add(tile.trim());
		}

		try {
			beginTask("Running Pig Script...");
			boolean successful = executeCommand(command, context);
			if (successful) {
				endTask("Execution successful.", Message.OK);
				return true;
			} else {
				endTask("Execution failed. Please have a look at the logfile for details.",
						Message.ERROR);
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

}
