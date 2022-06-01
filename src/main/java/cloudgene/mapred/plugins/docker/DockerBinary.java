package cloudgene.mapred.plugins.docker;

import cloudgene.mapred.util.BinaryFinder;
import cloudgene.mapred.util.Settings;
import genepi.hadoop.command.Command;
import genepi.io.FileUtil;

import java.io.File;

public class DockerBinary {

	private String binary = "";

	public static DockerBinary build(Settings settings) {
		String binary = new BinaryFinder("docker").settings(settings, "docker", "home").env("DOCKER_HOME")
				.envPath().find();
		return new DockerBinary(binary);
	}

	private DockerBinary(String binary) {
		this.binary = binary;
	}

	public String getBinary() {
		return binary;
	}

	public boolean isInstalled() {
		if (binary != null) {
			String binary = getBinary();
			return (new File(binary)).exists();
		} else {
			return false;
		}
	}

	public String getVersion() {
		if (isInstalled()) {
			String binary = getBinary();
			Command command = new Command(binary, "version");
			command.saveStdOut(FileUtil.path("docker-version.txt"));
			command.setSilent(true);
			command.execute();
			String version = FileUtil.readFileAsString("docker-version.txt");
			FileUtil.deleteFile("docker-version.txt");
			return version;
		} else {
			return "Docker not installed.";
		}
	}
}
