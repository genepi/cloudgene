package cloudgene.mapred.steps;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.jobs.Message;
import cloudgene.mapred.wdl.WdlStep;
import genepi.hadoop.common.WorkflowContext;
import genepi.io.FileUtil;

public class JavaExternalStep extends CloudgeneStep {

	protected Process process;

	protected static final Log log = LogFactory.getLog(JavaExternalStep.class);

	public boolean run(WdlStep step, CloudgeneContext context) {

		String javaPath = System.getProperty("java.home");

		File path = new File(javaPath);

		if (!path.exists()) {
			context.error("Java Binary was not found. Not found: " + javaPath);
			return false;
		}

		String javaBin = "";

		if (path.isDirectory()) {
			javaBin = FileUtil.path(javaPath, "bin", "java");
		} else {
			javaBin = javaPath;
		}

		File file = new File(javaBin);

		if (!file.exists()) {
			context.error("Java Binary was not found." + javaBin);
			return false;
		}

		if (!file.canExecute()) {
			context.error(
					"Java Binary was found (" + javaBin + ") but can not be executed. Please check the permissions.");
			return false;
		}

		String jar = step.getJar();
		String paramsString = step.getParams();
		String[] params = paramsString.split(" ");

		List<String> command = new Vector<String>();
		command.add(javaBin);
		command.add("-jar");
		command.add(jar);
		for (String param : params) {
			command.add(param.trim());
		}
		try {
			context.beginTask("Running Java Application...");
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

	protected boolean executeCommand(List<String> command, WorkflowContext context)
			throws IOException, InterruptedException {
		// set global variables
		for (int j = 0; j < command.size(); j++) {

			String cmd = command.get(j).replaceAll("\\$job_id", context.getJobId());
			command.set(j, cmd);
		}

		log.info(command);

		context.log("Command: " + command);
		context.log("Working Directory: " + new File(context.getWorkingDirectory()).getAbsolutePath());

		ProcessBuilder builder = new ProcessBuilder(command);
		builder.directory(new File(context.getWorkingDirectory()));
		builder.redirectErrorStream(true);
		process = builder.start();
		InputStream is = process.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String line = null;

		while ((line = br.readLine()) != null) {
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

	}

	public int getMapProgress() {
		return -1;
	}

	public int getReduceProgress() {
		return -1;
	}

	@Override
	public void kill() {

		process.destroy();

	}

}
