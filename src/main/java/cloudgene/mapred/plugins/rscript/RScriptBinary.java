package cloudgene.mapred.plugins.rscript;

import java.io.File;

import genepi.hadoop.command.Command;
import genepi.io.FileUtil;

public class RScriptBinary {

	public static final String[] PACKAGES = new String[] { "knitr", "markdown" };

	public static final String RSCRIPT_PATH = "/usr/bin/Rscript";

	public static boolean isInstalled() {

		return (new File(RSCRIPT_PATH)).exists();

	}

	public static String getVersion() {
		if (isInstalled()) {
			Command command = new Command(RSCRIPT_PATH, "--version");
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

	public static boolean isMarkdownInstalled() {
		if (isInstalled()) {

			RScriptFile script = new RScriptFile("verify.R");
			for (String pkg : PACKAGES) {
				script.append("library('" + pkg + "')");
			}
			script.save();
			Command command = new Command(RSCRIPT_PATH, "verify.R");
			command.setSilent(true);
			int result = command.execute();
			return result == 0;

		} else {
			return false;
		}
	}

	public static String getMarkdownDetails() {
		RScriptFile script = new RScriptFile("verify.R");
		for (String pkg : PACKAGES) {
			script.append("library('" + pkg + "')");
			script.append("'" + pkg + "'");
			script.append("packageVersion(\"" + pkg + "\")");
		}
		script.save();
		Command command = new Command(RSCRIPT_PATH, "verify.R");
		command.setSilent(true);
		command.saveStdOut(FileUtil.path("verify.txt"));
		command.execute();
		String output = FileUtil.readFileAsString("verify.txt");
		FileUtil.deleteFile("verify.txt");
		FileUtil.deleteFile("verify.R");
		return output;
	}

	public static String getMarkdownErrorDetails() {
		RScriptFile script = new RScriptFile("verify.R");
		for (String pkg : PACKAGES) {
			script.append("library('" + pkg + "')");
		}
		script.save();
		Command command = new Command(RSCRIPT_PATH, "verify.R");
		command.saveStdErr(FileUtil.path("verify.txt"));
		command.setSilent(true);
		command.execute();
		String output = FileUtil.readFileAsString("verify.txt");
		FileUtil.deleteFile("verify.txt");
		FileUtil.deleteFile("verify.R");
		return output;
	}
	
}
