package cloudgene.mapred.steps;

import java.io.File;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.jobs.Message;
import cloudgene.mapred.plugins.rscript.RMarkdownPlugin;
import cloudgene.mapred.plugins.rscript.RScriptBinary;
import cloudgene.mapred.plugins.rscript.RScriptFile;
import cloudgene.mapred.plugins.rscript.RScriptPlugin;
import cloudgene.mapred.util.command.Command;
import cloudgene.mapred.wdl.WdlStep;
import genepi.io.FileUtil;

public class RMarkdownLocalStep extends CloudgeneStep {

	@Override
	public boolean run(WdlStep step, CloudgeneContext context) {

		context.beginTask("Running Report Script...");

		String workingDirectory = context.getWorkingDirectory();

		String rmd = step.get("rmd");
		if (rmd == null || rmd.isEmpty()) {
			rmd = step.get("rmd2");
		}
		if (rmd == null || rmd.isEmpty()) {
			context.endTask("Execution failed. Please set the 'rmd' parameter.", Message.ERROR);
			return false;
		}
		String output = step.get("output");
		if (output == null || output.isEmpty()) {
			context.endTask("Execution failed. Please set the 'output' parameter.", Message.ERROR);
			return false;
		}

		String paramsString = step.get("params");
		String[] params = new String[] {};
		if (paramsString != null) {
			params = paramsString.split(" ");
		}

		String script = FileUtil.path(workingDirectory, rmd);
		context.log("Running script " + script + "...");
		context.log("Working Directory: " + workingDirectory);
		context.log("Output: " + output);
		context.log("Parameters:");
		for (String param : params) {
			context.log("  " + param);
		}

		int result = convert(script, output, params, context);

		if (result == 0) {
			context.endTask("Execution successful.", Message.OK);
			String include = step.get("include");
			if (include != null && include.equals("true")) {
				context.addFile(output);
			}
			return true;
		} else {
			context.endTask(
					"Execution failed. Please contact the server administrators for help if you believe this job should have completed successfully.",
					Message.ERROR);
			return false;
		}

	}

	public int convert(String rmdScript, String outputHtml, String[] args, CloudgeneContext context) {

		context.log("Creating RMarkdown report from " + rmdScript + "...");

		outputHtml = new File(outputHtml).getAbsolutePath();

		String folder = new File(outputHtml).getParentFile().getAbsolutePath() + "/figures-temp/";

		FileUtil.createDirectory(folder);

		String scriptFilename = "convert_" + System.currentTimeMillis() + ".R";

		RScriptFile script = new RScriptFile(scriptFilename);
		script.append("library(knitr)");
		script.append("library(markdown)");
		script.append("rmarkdown::render(\"" + rmdScript + "\", output_file=\"" + outputHtml + "\")");

		script.save();

		RScriptBinary rscript = RScriptBinary.build(context.getSettings());
		Command rScript = new Command(rscript.getBinary());
		rScript.setSilent(true);

		String[] argsForScript = new String[args.length + 1];
		argsForScript[0] = scriptFilename;
		// argsForScript[1] = "--args";
		for (int i = 0; i < args.length; i++) {
			argsForScript[i + 1] = args[i];
		}

		rScript.setParams(argsForScript);
		rScript.saveStdErr(FileUtil.path(folder, "std.err"));
		rScript.saveStdOut(FileUtil.path(folder, "std.out"));

		int result = rScript.execute();

		context.println(FileUtil.readFileAsString(FileUtil.path(folder, "std.err")));
		context.println(FileUtil.readFileAsString(FileUtil.path(folder, "std.out")));

		new File(outputHtml + ".md").delete();
		new File(scriptFilename).delete();

		RMarkdownLocalStep.deleteFolder(new File(folder));

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

	@Override
	public String[] getRequirements() {
		return new String[] { RScriptPlugin.ID, RMarkdownPlugin.ID };
	}

}
