package cloudgene.mapred.plugins.docker;

import java.io.File;

import genepi.hadoop.command.Command;
import genepi.io.FileUtil;

public class DockerBinary {

	public static final String BINARY_PATH = "/usr/bin/docker";

	public static boolean isInstalled() {
		return (new File(BINARY_PATH)).exists();
	}

	public static String getVersion() {
		if (isInstalled()) {
			Command command = new Command(BINARY_PATH, "version");
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
