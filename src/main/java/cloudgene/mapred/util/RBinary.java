package cloudgene.mapred.util;

import java.io.File;

import genepi.hadoop.command.Command;
import genepi.io.FileUtil;

public class RBinary {

	public static final String RSCRIPT_PATH = "/usr/bin/Rscript";

	public static boolean isInstalled() {

		return (new File(RSCRIPT_PATH)).exists();

	}

	public static String getVersion() {
		if (isInstalled()) {
			Command command = new Command(RSCRIPT_PATH, "--version");
			command.saveStdErr(FileUtil.path("r-version.txt"));
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
			return !getMarkdownDetails().contains("FALSE");
		} else {
			return false;
		}
	}

	public static String getMarkdownDetails() {
		MyRScript script = new MyRScript("verify.R");
		script.append("is.installed <- function(mypkg) is.element(mypkg, installed.packages()[,1])");
		script.append("is.installed('knitr') ");
		script.append("is.installed('markdown') ");
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
