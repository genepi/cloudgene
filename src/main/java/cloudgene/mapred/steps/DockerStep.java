package cloudgene.mapred.steps;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ExecCreation;
import com.spotify.docker.client.messages.ExecState;
import com.spotify.docker.client.messages.HostConfig;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.jobs.Message;
import cloudgene.mapred.util.Technology;
import cloudgene.mapred.wdl.WdlStep;
import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;

public class DockerStep extends CloudgeneStep {

	public static final String DOCKER_WORKSPACE = "/mnt/cloudgene";

	public static final String DOCKER_WORKING = "/mnt/working";

	
	@Override
	public boolean run(WdlStep step, CloudgeneContext context) {

		String cmd = step.get("cmd");

		if (cmd == null) {
			context.error("No 'exec' or 'cmd' parameter found.");
		}

		if (cmd.isEmpty()) {
			context.error("'exec' or 'cmd' parameter cannot be an empty string.");
		}

		String stdout = step.get("stdout", "false");
		boolean streamStdout = stdout.equals("true");

		String[] params = cmd.split(" ");

		String image = step.get("image");

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
		context.beginTask("Starting docker container...");

		String localWorkspace = new File(context.getJob().getLocalWorkspace()).getAbsolutePath();

		try {

			// replace all paths with paths in docker workspace
			String[] newParams = new String[cmd.length];
			for (int i = 0; i < newParams.length; i++) {
				String param = cmd[i];

				// checkout hdfs file
				if (param.startsWith("hdfs://")) {
					String name = FileUtil.getFilename(param);
					String localFile = FileUtil.path(((CloudgeneContext) context).getLocalTemp(), "local_" + name);
					try {
						HdfsUtil.checkOut(param, localFile);
						String localFilename = new File(localFile).getAbsolutePath();
						newParams[i] = localFilename;
					} catch (IOException e) {
						context.log(e.getMessage());
						newParams[i] = param.replaceAll(localWorkspace, DOCKER_WORKSPACE);
					}

				} else {
					newParams[i] = param.replaceAll(localWorkspace, DOCKER_WORKSPACE);
				}
			}

			// open docker connection
			DockerClient docker = DefaultDockerClient.fromEnv().build();

			if (!image.contains(":")) {
				image = image + ":latest";
			}

			// pull image
			docker.pull(image);

			// mount workspace from host to container
			String[] volumes = { localWorkspace + ":" + DOCKER_WORKSPACE,
					context.getWorkingDirectory() + ":" + DOCKER_WORKING };
			final HostConfig hostConfig = HostConfig.builder().privileged(false).binds(volumes).build();

			// create container
			final ContainerConfig containerConfig = ContainerConfig.builder().hostConfig(hostConfig)
					.cmd("sh", "-c", "while :; do sleep 1; done").image(image).build();

			ContainerCreation creation = docker.createContainer(containerConfig);

			String dockerId = creation.id();

			// start container
			docker.startContainer(dockerId);

			// execute command inside container
			context.log("Command: " + Arrays.toString(newParams));

			ExecCreation execCreation = docker.execCreate(dockerId, newParams,
					DockerClient.ExecCreateParam.attachStdout(), DockerClient.ExecCreateParam.attachStderr());
			LogStream output = docker.execStart(execCreation.id());
			String execOutput = output.readFully();

			// get exit code
			ExecState result = docker.execInspect(execCreation.id());

			// kill container and close docker client
			docker.killContainer(dockerId);
			docker.removeContainer(dockerId);
			docker.close();

			context.println(execOutput);

			if (result.exitCode() == 0) {

				if (streamStdout) {
					context.endTask(execOutput, Message.OK);
				} else {
					context.endTask("Execution successful.", Message.OK);
				}
				return true;

			} else {

				if (streamStdout) {
					context.endTask(execOutput, Message.ERROR);
				} else {
					context.endTask("Execution failed. Please have a look at the logfile for details.", Message.ERROR);
				}
				return false;

			}

		} catch (Exception e) {
			context.log("Execeution failed.", e);
			context.endTask("Execeution failed.", Message.ERROR);
			return false;
		}
	}

	@Override
	public Technology[] getRequirements() {
		return new Technology[] { Technology.DOCKER };
	}

}
