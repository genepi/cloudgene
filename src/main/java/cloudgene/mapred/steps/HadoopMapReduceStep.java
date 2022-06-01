package cloudgene.mapred.steps;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.jobs.Message;
import cloudgene.mapred.plugins.hadoop.HadoopPlugin;
import cloudgene.mapred.wdl.WdlStep;
import cloudgene.sdk.internal.WorkflowContext;
import genepi.hadoop.HadoopUtil;
import genepi.io.FileUtil;

import java.io.*;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HadoopMapReduceStep extends CloudgeneStep {

	private String jobId;

	private int map = 0;

	private int reduce = 0;

	@Override
	public boolean run(WdlStep step, CloudgeneContext context) {

		String hadoopPath = context.getSettings().getHadoopPath();

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
			context.error(
					"Hadoop Binary was found (" + hadoop + ") but can not be executed. Please check the permissions.");
			return false;
		}

		String streamingJar = context.getSettings().getStreamingJar();

		// params
		String paramsString = step.get("params");
		String[] params = new String[] {};
		if (paramsString != null) {
			params = paramsString.split(" ");
		}

		// hadoop jar or streaming
		List<String> command = new Vector<String>();

		command.add(hadoop);
		
		// -fs and -jt and -config are supported by generic tool runner.

		command.add("jar");

		if (step.getJar() != null) {

			// classical
			command.add(step.getJar());

		} else {

			// streaming

			if (context.getSettings().isStreaming()) {

				command.add(streamingJar);

			} else {

				context.error(
						"Streaming mode is disabled.\nPlease specify the streaming-jar file in config/settings.yaml to run this job..");
				return false;

			}

		}

		for (String tile : params) {
			command.add(tile.trim());
		}

		// mapper and reducer

		if (step.getJar() == null) {

			if (step.get("mapper") != null) {

				String tiles[] = step.get("mapper").split(" ", 2);
				String filename = tiles[0];

				command.add("-mapper");

				if (tiles.length > 1) {
					String params2 = tiles[1];
					command.add(filename + " " + params2);
				} else {
					command.add(filename);
				}

			}

			if (step.get("reducer") != null) {

				String tiles[] = step.get("reducer").split(" ", 2);
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
				context.endTask("Execution failed. Please contact the server administrators for help if you believe this job should have completed successfully.", Message.ERROR);
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	@Override
	protected boolean executeCommand(List<String> command, WorkflowContext context)
			throws IOException, InterruptedException {
		// set global variables
		for (int j = 0; j < command.size(); j++) {

			String cmd = command.get(j).replaceAll("\\$job_id", context.getJobId());
			command.set(j, cmd);
		}

		context.log("Command: " + command);
		context.log("Working Directory: " + new File(context.getWorkingDirectory()).getAbsolutePath());

		ProcessBuilder builder = new ProcessBuilder(command);
		builder.directory(new File(context.getWorkingDirectory()));
		builder.redirectErrorStream(true);
		Process process = builder.start();
		InputStream is = process.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String line = null;

		// Find job id and write output into file
		Pattern pattern = Pattern.compile("Running job: (.*)");
		Pattern pattern2 = Pattern.compile("HadoopJobId: (.*)");

		while ((line = br.readLine()) != null) {

			Matcher matcher = pattern.matcher(line);
			if (matcher.find()) {
				// write statistics from old job
				jobId = matcher.group(1).trim();
				context.log("Job " + context.getJobId() + " -> HadoopJob " + jobId);
			} else {
				Matcher matcher2 = pattern2.matcher(line);
				if (matcher2.find()) {
					jobId = matcher2.group(1).trim();
					context.log("Job " + context.getJobId() + " -> HadoopJob " + jobId);
				}
			}

			context.println(line);
		}

		br.close();
		isr.close();
		is.close();

		process.waitFor();
		context.log("Exit Code: " + process.exitValue());

		if (process.exitValue() != 0) {
			return false;
		} else {
			process.destroy();
		}
		return true;
	}

	@Override
	public void updateProgress() {

		/*RunningJob job = HadoopUtil.getInstance().getJob(jobId);
		if (job != null) {

			try {

				if (job.setupProgress() >= 1) {
					map = ((int) (job.mapProgress() * 100));
					reduce = ((int) (job.reduceProgress() * 100));
				} else {
					map = 0;
					reduce = 0;
				}

			} catch (Exception e) {
				map = 0;
				reduce = 0;
			}

		} else {
			map = 0;
			reduce = 0;
		}*/

	}

	@Override
	public int getProgress() {
		return map / 2 + reduce / 2;
	}

	@Override
	public void kill() {

		try {

			if (jobId != null) {

				context.log(" Cancel Job " + jobId);

				HadoopUtil.getInstance().kill(jobId);

			}

		} catch (IOException e) {

			context.log(" Cancel Job failed: " + e);

		}

	}

	@Override
	public String[] getRequirements() {
		return new String[] { HadoopPlugin.ID };
	}

}
