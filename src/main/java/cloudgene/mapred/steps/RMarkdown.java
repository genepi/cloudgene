package cloudgene.mapred.steps;

import genepi.io.FileUtil;

import java.io.File;
import java.io.IOException;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.jobs.Message;
import cloudgene.mapred.util.HdfsUtil;
import cloudgene.mapred.util.rscript.MyRScript;
import cloudgene.mapred.util.rscript.RScript;
import cloudgene.mapred.wdl.WdlStep;

public class RMarkdown extends CloudgeneStep {

	@Override
	public boolean run(WdlStep step, CloudgeneContext context) {

		beginTask("Running Report Script...");

		String wd = context.getWorkingDirectory();

		String rmd = step.getRmd();
		String output = step.getOutput();
		String paramsString = step.getParams();
		String[] params = paramsString.split(" ");

		context.println("Running script " + step.getRmd() + "...");
		context.println("Working Directory: " + wd);
		context.println("Output: " + output);
		context.println("Parameters:");
		for (String param : params) {
			context.println("  " + param);
		}

		int result = convert(FileUtil.path(wd, rmd), output, params, context);

		if (result == 0) {
			endTask("Execution successful.", Message.OK);
			return true;
		} else {
			endTask("Execution failed. Please have a look at the logfile for details.",
					Message.ERROR);
			return false;
		}

	}

	public int convert(String rmdScript, String outputHtml, String[] args,
			CloudgeneContext context) {

		context.println("Creating RMarkdown report from " + rmdScript + "...");

		outputHtml = new File(outputHtml).getAbsolutePath();

		String folder = new File(outputHtml).getParentFile().getAbsolutePath()
				+ "/figures-temp/";

		FileUtil.createDirectory(folder);

		MyRScript script = new MyRScript("convert.R");
		script.append("library(knitr)");
		script.append("opts_chunk$set(fig.path='" + folder + "')");
		script.append("library(markdown)");
		script.append("knit(\"" + rmdScript + "\", \"" + outputHtml + ".md\")");
		script.append("markdownToHTML(\"" + outputHtml + ".md\", \""
				+ outputHtml + "\")");
		script.save();

		RScript rScript = new RScript();
		rScript.setSilent(false);

		String[] argsForScript = new String[args.length + 1];
		argsForScript[0] = "convert.R";
		// argsForScript[1] = "--args";
		for (int i = 0; i < args.length; i++) {

			// checkout hdfs file
			if (args[i].startsWith("hdfs://")) {

				String localFile = FileUtil.path(folder, "local_file_" + i);
				context.println("Check out file " + args[i] + "...");
				try {
					HdfsUtil.checkOut(args[i], localFile);
					argsForScript[i + 1] = localFile;
				} catch (IOException e) {
					context.println(e.getMessage());
					argsForScript[i + 1] = args[i];
				}

			} else {

				argsForScript[i + 1] = args[i];

			}
		}

		rScript.setParams(argsForScript);
		int result = rScript.execute();

		new File(outputHtml + ".md").delete();
		new File("convert.R").delete();
		RMarkdown.deleteFolder(new File(folder));

		return result;

	}

	public static void deleteFolder(File folder) {
		File[] files = folder.listFiles();
		if (files != null) { // some JVMs return null for empty dirs
			for (File f : files) {
				if (f.isDirectory()) {
					deleteFolder(f);
				} else {
					f.delete();
				}
			}
		}
		folder.delete();
	}

}
