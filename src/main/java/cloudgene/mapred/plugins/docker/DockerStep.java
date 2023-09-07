package cloudgene.mapred.plugins.docker;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.jobs.Message;
import cloudgene.mapred.wdl.WdlStep;

public class DockerStep extends CloudgeneStep {

	public static final String DOCKER_WORKSPACE = "/mnt/cloudgene";

	public static final String DOCKER_WORKING = "/mnt/working";

	@Override
	public boolean run(WdlStep step, CloudgeneContext context) {

		String cmd = step.getString("cmd");

		if (cmd == null) {
			context.error("No 'exec' or 'cmd' parameter found.");
		}

		if (cmd.isEmpty()) {
			context.error("'exec' or 'cmd' parameter cannot be an empty string.");
		}

		String stdout = step.getString("stdout", "false");
		boolean streamStdout = stdout.equals("true");

		String[] params = cmd.split(" ");

		String image = step.getString("image");

		if (image == null) {
			context.error("No 'image' parameter found.");
		}

		if (image.isEmpty()) {
			context.error("'image' parameter cannot be an empty string.");
		}

		return runInDockerContainer(context, image, params, streamStdout);

	}

	protected boolean runInDockerContainer(CloudgeneContext context, String image, String[] cmd) {
		return runInDockerContainer(context, image, cmd, false);
	}

	protected boolean runInDockerContainer(CloudgeneContext context, String image, String[] cmd, boolean streamStdout) {

		String localWorkspace = new File(context.getJob().getLocalWorkspace()).getAbsolutePath();

		try {

			// replace all paths with paths in docker workspace
			String[] newParams = new String[cmd.length];
			for (int i = 0; i < newParams.length; i++) {
				String param = cmd[i];
				newParams[i] = param.replaceAll(localWorkspace, DOCKER_WORKSPACE);
			}

			if (!image.contains(":")) {
				image = image + ":latest";
			}

			// mount workspace from host to container
			String[] volumes = { localWorkspace + ":" + DOCKER_WORKSPACE,
					context.getWorkingDirectory() + ":" + DOCKER_WORKING };
			context.log("Command: " + Arrays.toString(newParams));

			DockerBinary binary = DockerBinary.build(context.getSettings());
			DockerCommandBuilder builder = new DockerCommandBuilder(binary);
			List<String> command = builder.image(image).binds(volumes).command(newParams).build();

			StringBuilder output = null;
			if (streamStdout) {
				output = new StringBuilder();
			}

			try {
				context.beginTask("Running Command...");
				boolean successful = executeCommand(command, context, output);
				if (successful) {
					if (streamStdout) {
						context.endTask(output.toString(), Message.OK);
					} else {
						context.endTask("Execution successful.", Message.OK);
					}
					return true;
				} else {
					if (streamStdout) {
						context.endTask(output.toString(), Message.ERROR);
					} else {
						context.endTask("Execution failed. Please contact the server administrators for help if you believe this job should have completed successfully.",
								Message.ERROR);
					}
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}

		} catch (Exception e) {
			context.log("Execution failed.", e);
			context.endTask("Execution failed.", Message.ERROR);
			return false;
		}
	}

	@Override
	public String[] getRequirements() {
		return new String[] { DockerPlugin.ID };
	}

}
