package cloudgene.mapred.plugins.nextflow;

import java.io.File;

import cloudgene.mapred.util.BinaryFinder;
import cloudgene.mapred.util.Settings;
import genepi.hadoop.command.Command;
import genepi.io.FileUtil;

public class NextflowBinary {

	private String binary = "";

	public static NextflowBinary build(Settings settings) {
		String binary = new BinaryFinder("nextflow").settings(settings, "nextflow", "home").env("NEXTFLOW_HOME")
				.envPath().path("/usr/local/bin").find();
		return new NextflowBinary(binary);
	}

	private NextflowBinary(String binary) {
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
			Command command = new Command(binary, "info");
			command.saveStdOut(FileUtil.path("info-version.txt"));
			command.setSilent(true);
			command.execute();
			String version = FileUtil.readFileAsString("info-version.txt");
			FileUtil.deleteFile("info-version.txt");
			return version;
		} else {
			return "Nextflow not installed.";
		}
	}
}
