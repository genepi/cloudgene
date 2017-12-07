package cloudgene.mapred.steps;

import genepi.hadoop.common.WorkflowContext;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.Message;
import cloudgene.mapred.util.Technology;
import cloudgene.mapred.wdl.WdlStep;

public class SparkStep extends Hadoop {

	@Override
	public boolean run(WdlStep step, CloudgeneContext context) {

		String pigPath = context.getSettings().getSparkPath();

		// params
		String paramsString = step.getParams();
		String[] params = paramsString.split(" ");

		// spark script
		List<String> command = new Vector<String>();

		command.add(pigPath);
		command.add("--class");
		command.add(step.getMainClass());
		command.add("--master");
		command.add("yarn");
		command.add(step.getSpark());

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
				context.endTask("Execution failed. Please have a look at the logfile for details.",
						Message.ERROR);
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	protected boolean ex(List<String> command, WorkflowContext context)
			throws IOException, InterruptedException {
	

		log.info(command);

		context.log("Command: " + command);
		context.log("Working Directory: "
				+ new File(context.getWorkingDirectory()).getAbsolutePath());

		ProcessBuilder builder = new ProcessBuilder(command);
		builder.directory(new File(context.getWorkingDirectory()));
		builder.redirectErrorStream(true);
		Process process = builder.start();

		process.waitFor();
		context.log("Exit Code: " + process.exitValue());

		return true;
	}
	
	@Override
	public Technology[] getRequirements() {
		return new Technology[]{Technology.HADOOP_CLUSTER};
	}
}
