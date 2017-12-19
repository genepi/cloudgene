package cloudgene.mapred.util;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ExecCreation;

public class DockerHadoopCluster {

	private String dockerId;

	private String ipAddress;

	private String name;

	public boolean start(String image) throws DockerCertificateException, DockerException, InterruptedException {

		System.out.println("Start docker Hadoop cluster using image " + image + "...");

		pullDependencyContainer(image);

		DockerClient docker = DefaultDockerClient.fromEnv().build();

		// Create container with exposed ports
		final ContainerConfig containerConfig = ContainerConfig.builder().image(image).hostname("hadoop-docker")
				.cmd("sh", "-c", "while :; do sleep 1; done").build();

		ContainerCreation creation = docker.createContainer(containerConfig);

		dockerId = creation.id();

		// Inspect container
		ContainerInfo info = docker.inspectContainer(dockerId);
		System.out.println("Starting container " + info.name() + "...");
		name = info.name();
		// Start container
		docker.startContainer(dockerId);

		System.out.println("Executing initial script... ");

		String[] command = { "bash", "-c", "run-hadoop-initial.sh" };
		ExecCreation execCreation = docker.execCreate(dockerId, command, DockerClient.ExecCreateParam.attachStdout(),
				DockerClient.ExecCreateParam.attachStderr());
		LogStream output = docker.execStart(execCreation.id());
		String execOutput = output.readFully();

		System.out.println("Starting container [OK] ");

		info = docker.inspectContainer(dockerId);
		ipAddress = info.networkSettings().ipAddress();

		System.out.println("Cluster is running on " + ipAddress);
		System.out.println();

		docker.close();

		return true;

	}

	public boolean pullDependencyContainer(String image) throws DockerCertificateException {
		try {
			System.out.println("Pull Docker Image " + image + "...");
			DockerClient docker = DefaultDockerClient.fromEnv().build();
			docker.pull(image + ":latest");
			System.out.println("Pull Docker Image " + image + " [OK]");
			docker.close();
			return true;
		} catch (DockerException e) {
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean isRunning() {
		try {
			DockerClient docker = DefaultDockerClient.fromEnv().build();
			ContainerInfo info = docker.inspectContainer(dockerId);
			docker.close();

			if (info != null) {
				return info.state().running();
			} else {
				return false;
			}
		} catch (DockerCertificateException e) {
			return false;
		} catch (DockerException e) {
			// e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			// e.printStackTrace();
			return false;
		}
	}

	public boolean stop() throws DockerCertificateException {
		try {

			if (!isRunning()) {
				System.out.println("Docker Hadoop cluster " + name + " is not running.");

			}

			System.out.println("Stop docker Hadoop cluster " + name + "...");

			DockerClient docker = DefaultDockerClient.fromEnv().build();
			docker.killContainer(dockerId);
			docker.removeContainer(dockerId);
			docker.close();
			System.out.println("Stop docker Hadoop cluster [OK]");
			return true;

		} catch (DockerException e) {
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
	}

	public String getIpAddress() {
		return ipAddress;
	}

}
