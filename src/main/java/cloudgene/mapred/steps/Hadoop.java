package cloudgene.mapred.steps;

import genepi.io.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapred.RunningJob;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.util.HadoopUtil;
import cloudgene.mapred.util.Settings;

public abstract class Hadoop extends CloudgeneStep {

	protected String jobId;

	protected static final Log log = LogFactory.getLog(Hadoop.class);

	protected int map = 0;

	protected int reduce = 0;

	protected boolean executeJar(CloudgeneContext context, String jar,
			String... params) throws IOException, InterruptedException {

		String hadoopPath = Settings.getInstance().getHadoopPath();

		File path = new File(hadoopPath);

		if (!path.exists()) {
			error("Hadoop Binary was not found. Please set the correct path in the admin panel.");
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
			error("Hadoop Binary was not found. Please set the correct path in the admin panel.");
			return false;
		}

		if (!file.canExecute()) {
			error("Hadoop Binary was found ("
					+ hadoop
					+ ") but can not be executed. Please check the permissions.");
			return false;
		}

		// hadoop jar or streaming
		List<String> command = new Vector<String>();

		command.add(hadoop);
		command.add("jar");
		command.add(jar);
		for (String param : params) {
			command.add(param.trim());
		}
		return executeCommand(command, context);
	}

	protected boolean executeCommand(List<String> command,
			CloudgeneContext context) throws IOException, InterruptedException {
		// set global variables
		for (int j = 0; j < command.size(); j++) {

			String cmd = command.get(j).replaceAll("\\$job_id",
					context.getJob().getId());
			command.set(j, cmd);
		}

		log.info(command);

		context.println("Command: " + command);
		context.println("Working Directory: "
				+ new File(context.getWorkingDirectory()).getAbsolutePath());

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

		context.println("Output: ");
		while ((line = br.readLine()) != null) {

			Matcher matcher = pattern.matcher(line);
			if (matcher.find()) {
				// write statistics from old job
				jobId = matcher.group(1).trim();
				log.info("Job " + context.getJob().getId() + " -> HadoopJob "
						+ jobId);
			} else {
				Matcher matcher2 = pattern2.matcher(line);
				if (matcher2.find()) {
					jobId = matcher2.group(1).trim();
					log.info("Job " + context.getJob().getId()
							+ " -> HadoopJob " + jobId);
				}
			}

			context.println("  " + line);
		}

		br.close();
		isr.close();
		is.close();

		process.waitFor();
		context.println("Exit Code: " + process.exitValue());

		if (process.exitValue() != 0) {
			return false;
		} else {
			process.destroy();
		}
		return true;
	}

	@Override
	public void updateProgress() {

		RunningJob job = HadoopUtil.getInstance().getJob(jobId);
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
		}

	}

	public int getMapProgress() {
		return map;
	}

	public int getReduceProgress() {
		return reduce;
	}

	@Override
	public void kill() {

		try {

			if (jobId != null) {

				log.info(" Cancel Job " + jobId);

				HadoopUtil.getInstance().kill(jobId);

			}

		} catch (IOException e) {

			log.error(" Cancel Job failed: ", e);

		}

	}

}
