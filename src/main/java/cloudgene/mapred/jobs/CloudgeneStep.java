package cloudgene.mapred.jobs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import cloudgene.mapred.util.Technology;
import cloudgene.mapred.wdl.WdlStep;
import genepi.hadoop.common.WorkflowContext;

public abstract class CloudgeneStep {

	private int id;

	private String name;

	private AbstractJob job;

	private List<Message> logMessages;

	public CloudgeneStep() {

	}

	public String getFolder(Class clazz) {
		return new File(clazz.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public AbstractJob getJob() {
		return job;
	}

	public void setJob(AbstractJob job) {
		this.job = job;
	}

	public void setup(CloudgeneContext context) {

	}

	public boolean run(WdlStep step, CloudgeneContext context) {
		return true;
	}

	public int getProgress() {
		return -1;
	}

	public void updateProgress() {

	}

	public void kill() {

	}

	public List<Message> getLogMessages() {
		return logMessages;
	}

	public void setLogMessages(List<Message> logMessages) {
		this.logMessages = logMessages;
	}

	public Technology[] getRequirements() {
		return new Technology[] {};
	}

	// dummy for beam serialization (setup --> property up!)
	public CloudgeneContext getup() {
		return null;
	}

	protected boolean executeCommand(List<String> command, WorkflowContext context)
			throws IOException, InterruptedException {
		return executeCommand(command, context, null);
	}

	protected boolean executeCommand(List<String> command, WorkflowContext context, StringBuilder output)
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

		while ((line = br.readLine()) != null) {
			context.println(line);
			if (output != null) {
				output.append(line + "\n");
			}
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

}
