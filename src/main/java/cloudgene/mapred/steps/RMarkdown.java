package cloudgene.mapred.steps;

import genepi.hadoop.HdfsUtil;
import genepi.hadoop.command.Command;
import genepi.hadoop.common.WorkflowContext;
import genepi.io.FileUtil;

import java.io.File;
import java.io.IOException;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.jobs.Message;
import cloudgene.mapred.util.MyRScript;
import cloudgene.mapred.wdl.WdlStep;

public class RMarkdown extends CloudgeneStep {

	@Override
	public boolean run(WdlStep step, CloudgeneContext context) {

		context.beginTask("Running Report Script...");

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
			context.endTask("Execution successful.", Message.OK);
			return true;
		} else {
			context.endTask(
					"Execution failed. Please have a look at the logfile for details.",
					Message.ERROR);
			return false;
		}

	}

	public int convert(String rmdScript, String outputHtml, String[] args,
			WorkflowContext context) {

		context.println("Creating RMarkdown report from " + rmdScript + "...");

		outputHtml = new File(outputHtml).getAbsolutePath();

		String folder = new File(outputHtml).getParentFile().getAbsolutePath()
				+ "/figures-temp/";

		FileUtil.createDirectory(folder);

		String scriptFilename = "convert_" + System.currentTimeMillis() + ".R";

		MyRScript script = new MyRScript(scriptFilename);
		script.append("library(knitr)");
		script.append("opts_chunk$set(fig.path='" + folder + "')");
		script.append("library(markdown)");
		script.append("knit(\"" + rmdScript + "\", \"" + outputHtml + ".md\")");
		script.append("markdownToHTML(\"" + outputHtml + ".md\", \""
				+ outputHtml + "\")");
		script.save();

		Command rScript = new Command("/usr/bin/Rscript");
		rScript.setSilent(true);

		String[] argsForScript = new String[args.length + 1];
		argsForScript[0] = scriptFilename;
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

				try {
					context.println("Number of lines: "
							+ FileUtil.getLineCount(localFile));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {

				argsForScript[i + 1] = args[i];

			}
		}

		rScript.setParams(argsForScript);
		rScript.saveStdErr(FileUtil.path(folder, "std.err"));
		rScript.saveStdOut(FileUtil.path(folder, "std.out"));

		int result = rScript.execute();

		context.println(FileUtil.readFileAsString(FileUtil.path(folder,
				"std.err")));
		context.println(FileUtil.readFileAsString(FileUtil.path(folder,
				"std.out")));

		new File(outputHtml + ".md").delete();
		new File(scriptFilename).delete();

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
