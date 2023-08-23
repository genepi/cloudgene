package cloudgene.mapred.plugins.rscript;

import java.io.File;

import cloudgene.mapred.util.BinaryFinder;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.util.command.Command;
import genepi.io.FileUtil;

public class RScriptBinary {

	public static final String[] PACKAGES = new String[] { "knitr", "markdown" };

	private String binary = "";

	public static RScriptBinary build(Settings settings) {
		String binary = new BinaryFinder("Rscript").settings(settings, "rscript", "home").env("R_HOME").envPath()
				.find();
		return new RScriptBinary(binary);
	}

	private RScriptBinary(String binary) {
		this.binary = binary;
	}

	public String getBinary() {
		return binary;
	}

	public boolean isInstalled() {
		if (binary != null) {
			return (new File(binary)).exists();
		} else {
			return false;
		}
	}

	public String getVersion() {
		if (isInstalled()) {
			String binary = getBinary();
			Command command = new Command(binary, "--version");
			command.saveStdErr(FileUtil.path("r-version.txt"));
			command.setSilent(true);
			command.execute();
			String version = FileUtil.readFileAsString("r-version.txt");
			FileUtil.deleteFile("r-version.txt");
			return version;
		} else {
			return "R scripting front-end not installed.";
		}
	}

	public boolean isMarkdownInstalled() {
		if (isInstalled()) {
			String binary = getBinary();
			RScriptFile script = new RScriptFile("verify.R");
			for (String pkg : PACKAGES) {
				script.append("library('" + pkg + "')");
			}
			script.save();
			Command command = new Command(binary, "verify.R");
			command.setSilent(true);
			int result = command.execute();
			return result == 0;

		} else {
			return false;
		}
	}

	public String getMarkdownDetails() {
		RScriptFile script = new RScriptFile("verify.R");
		for (String pkg : PACKAGES) {
			script.append("library('" + pkg + "')");
			script.append("'" + pkg + "'");
			script.append("packageVersion(\"" + pkg + "\")");
		}
		script.save();
		String binary = getBinary();
		Command command = new Command(binary, "verify.R");
		command.setSilent(true);
		command.saveStdOut(FileUtil.path("verify.txt"));
		command.execute();
		String output = FileUtil.readFileAsString("verify.txt");
		FileUtil.deleteFile("verify.txt");
		FileUtil.deleteFile("verify.R");
		return output;
	}

	public String getMarkdownErrorDetails() {
		RScriptFile script = new RScriptFile("verify.R");
		for (String pkg : PACKAGES) {
			script.append("library('" + pkg + "')");
		}
		script.save();
		String binary = getBinary();
		Command command = new Command(binary, "verify.R");
		command.saveStdErr(FileUtil.path("verify.txt"));
		command.setSilent(true);
		command.execute();
		String output = FileUtil.readFileAsString("verify.txt");
		FileUtil.deleteFile("verify.txt");
		FileUtil.deleteFile("verify.R");
		return output;
	}

}
