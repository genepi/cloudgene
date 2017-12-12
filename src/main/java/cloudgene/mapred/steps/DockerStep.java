package cloudgene.mapred.steps;

import java.io.File;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ExecCreation;
import com.spotify.docker.client.messages.HostConfig;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.jobs.Message;
import cloudgene.mapred.wdl.WdlStep;

public class DockerStep extends CloudgeneStep {

	public static final String DOCKER_WORKSPACE = "/mnt/cloudgene";

	@Override
	public boolean run(WdlStep step, CloudgeneContext context) {

		String cmd = step.get("cmd");

		if (cmd == null) {
			context.error("No 'exec' or 'cmd' parameter found.");
		}

		if (cmd.isEmpty()) {
			context.error("'exec' or 'cmd' parameter cannot be an empty string.");
		}

		String[] params = cmd.split(" ");

		try {
			String image = step.get("image");

			if (image == null) {
				context.error("No 'image' parameter found.");
			}

			if (image.isEmpty()) {
				context.error("'image' parameter cannot be an empty string.");
			}

			DockerClient docker = DefaultDockerClient.fromEnv().build();

			if (!image.contains(":")) {
				image = image + ":latest";
			}
			docker.pull(image);

			String localWorkspace = new File(context.getJob().getLocalWorkspace()).getAbsolutePath();

			String[] volumes = { localWorkspace + ":" + DOCKER_WORKSPACE };

			final HostConfig hostConfig = HostConfig.builder().privileged(false).binds(volumes).build();

			// replace all paths with paths in docker workspace
			String[] newParams = new String[params.length];
			for (int i = 0; i < newParams.length; i++) {
				newParams[i] = params[i].replaceAll(localWorkspace, DOCKER_WORKSPACE);
			}

			// Create container with exposed ports
			final ContainerConfig containerConfig = ContainerConfig.builder().hostConfig(hostConfig)
					.cmd("sh", "-c", "while :; do sleep 1; done").image(image).build();

			ContainerCreation creation = docker.createContainer(containerConfig);

			String dockerId = creation.id();

			// Inspect container

			context.beginTask("Starting docker container...");

			// Start container
			docker.startContainer(dockerId);
			context.log("Command: " + newParams);

			ExecCreation execCreation = docker.execCreate(dockerId, newParams,
					DockerClient.ExecCreateParam.attachStdout(), DockerClient.ExecCreateParam.attachStderr());
			LogStream output = docker.execStart(execCreation.id());
			String execOutput = output.readFully();
			System.out.println("Starting container [OK] ");

			docker.killContainer(dockerId);
			docker.removeContainer(dockerId);

			docker.close();

			context.endTask(execOutput, Message.OK);

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

}
