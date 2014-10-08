package cloudgene.mapred.steps;

import genepi.hadoop.common.WorkflowContext;
import genepi.io.FileUtil;

import java.io.File;
import java.util.List;
import java.util.Vector;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.Message;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlStep;

public class MapReduce extends Hadoop {

	public void setup(CloudgeneContext context) {

	}

	@Override
	public boolean run(WdlStep step, WorkflowContext context) {

		String hadoopPath = Settings.getInstance().getHadoopPath();

		File path = new File(hadoopPath);

		if (!path.exists()) {
			context.error("Hadoop Binary was not found. Please set the correct path in the admin panel.");
			return false;
		}

		String hadoop = "";

		if (path.isDirectory()) {
			hadoop = FileUtil.path(hadoopPath, "bin", "hadoop");
		} else {
			hadoop = hadoopPath;
		}

		File file = new File(hadoop);

		if (!file.exists()) {
			context.error("Hadoop Binary was not found. Please set the correct path in the admin panel.");
			return false;
		}

		if (!file.canExecute()) {
			context.error("Hadoop Binary was found ("
					+ hadoop
					+ ") but can not be executed. Please check the permissions.");
			return false;
		}

		String streamingJar = Settings.getInstance().getStreamingJar();

		// params
		String paramsString = step.getParams();
		String[] params = paramsString.split(" ");

		// hadoop jar or streaming
		List<String> command = new Vector<String>();

		command.add(hadoop);
		command.add("jar");

		if (step.getJar() != null) {

			// classical
			command.add(step.getJar());

		} else {

			// streaming

			if (Settings.getInstance().isStreaming()) {

				command.add(streamingJar);

			} else {

				context.error("Htreaming mode is disabled.\nPlease specify the streaming-jar file in config/settings.yaml to run this job..");
				return false;

			}

		}

		for (String tile : params) {
			command.add(tile.trim());
		}

		// mapper and reducer

		if (step.getJar() == null) {

			if (step.getMapper() != null) {

				String tiles[] = step.getMapper().split(" ", 2);
				String filename = tiles[0];

				command.add("-mapper");

				if (tiles.length > 1) {
					String params2 = tiles[1];
					command.add(filename + " " + params2);
				} else {
					command.add(filename);
				}

			}

			if (step.getReducer() != null) {

				String tiles[] = step.getReducer().split(" ", 2);
				String filename = tiles[0];

				command.add("-reducer");

				if (tiles.length > 1) {
					String params2 = tiles[1];
					command.add(filename + " " + params2);
				} else {
					command.add(filename);
				}

			}

		}

		try {
			context.beginTask("Running Hadoop Job...");
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

}
