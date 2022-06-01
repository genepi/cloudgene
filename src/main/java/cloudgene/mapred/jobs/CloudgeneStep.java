package cloudgene.mapred.jobs;

import cloudgene.mapred.wdl.WdlStep;
import cloudgene.sdk.internal.WorkflowContext;

import java.io.*;
import java.util.List;

public abstract class CloudgeneStep {

	private int id;

	private String name;

	private AbstractJob job;

	private List<Message> logMessages;

	protected Process process;

	protected CloudgeneContext context;

	protected boolean killed = false;

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
		this.context = context;
	}

	public boolean run(WdlStep step, CloudgeneContext context) {
		return true;
	}

	public int getProgress() {
		return -1;
	}

	public void updateProgress() {

	}

	public List<Message> getLogMessages() {
		return logMessages;
	}

	public void setLogMessages(List<Message> logMessages) {
		this.logMessages = logMessages;
	}

	public String[] getRequirements() {
		return new String[] {};
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
		builder.redirectOutput();
		process = builder.start();
		InputStream is = process.getInputStream();
		InputStreamReader isr = new InputStreamReader(is, "ISO-8859-1");
		BufferedReader br = new BufferedReader(isr);
		String line = null;
		try {

			while ((line = br.readLine()) != null) {
				context.println(line);
				if (output != null) {
					output.append(line + "\n");
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
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

	public void kill() {
		if (process != null && process.isAlive()) {
			process.destroy();
			while (process.isAlive()) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			killed = true;
			context.log("Process killed by used.");
		}
	}

}
