package cloudgene.mapred.steps;

import java.io.File;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.plugins.docker.DockerPlugin;
import cloudgene.mapred.plugins.docker.DockerStep;
import cloudgene.mapred.plugins.rscript.RScriptFile;
import cloudgene.mapred.wdl.WdlStep;
import genepi.io.FileUtil;

public class RMarkdown2DockerStep extends DockerStep {

	public static final String DOCKER_R_BASE_IMAGE = "genepi/r-cran-docker:latest";

	@Override
	public boolean run(WdlStep step, CloudgeneContext context) {

		String workingDirectory = context.getWorkingDirectory();

		String rmd = step.get("rmd");
		if (rmd == null || rmd.isEmpty()) {
			context.error("Execution failed. Please set the 'rmd' parameter.");
			return false;
		}
		String output = step.get("output");
		if (output == null || output.isEmpty()) {
			context.error("Execution failed. Please set the 'output' parameter.");
			return false;
		}

		String image = step.get("image");
		if (image == null || image.isEmpty()) {
			image = DOCKER_R_BASE_IMAGE;
		}

		
		String paramsString = step.get("params");
		String[] params = new String[] {};
		if (paramsString != null) {
			params = paramsString.split(" ");
		}

		String script = FileUtil.path(rmd);
		context.log("Running script " + script + "...");
		context.log("Working Directory: " + workingDirectory);
		context.log("Output: " + output);
		context.log("Parameters:");
		for (String param : params) {
			context.log("  " + param);
		}

		return convert(script, image, output, params, context);

	}

	public boolean convert(String rmdScript, String image, String outputHtml, String[] args, CloudgeneContext context) {
		
		String localWorkspace = new File(context.getJob().getLocalWorkspace()).getAbsolutePath();

		context.log("Creating RMarkdown report from " + rmdScript + "...");

		outputHtml = new File(outputHtml).getAbsolutePath();
		outputHtml = outputHtml.replaceAll(localWorkspace, DOCKER_WORKSPACE);

		String folder = new File(outputHtml).getParentFile().getAbsolutePath() + "/figures-temp/";

		FileUtil.createDirectory(folder);

		String scriptFilename = FileUtil.path(localWorkspace, "convert_" + System.currentTimeMillis() + ".R");

		RScriptFile script = new RScriptFile(scriptFilename);
		script.append("library(knitr)");
		script.append("library(markdown)");
		// set working directory
		script.append("setwd(\"" + DOCKER_WORKING + "\")");
		script.append("rmarkdown::render(\"" + rmdScript + "\", output_file=\"" + outputHtml + "\")");

		script.save();
		scriptFilename = scriptFilename.replaceAll(localWorkspace, DOCKER_WORKSPACE);

		String[] argsForScript = new String[args.length + 2];
		argsForScript[0] = "Rscript";
		argsForScript[1] = scriptFilename;

		// argsForScript[1] = "--args";
		for (int i = 0; i < args.length; i++) {
			argsForScript[i + 2] = args[i];
		}

		boolean result = runInDockerContainer(context, image, argsForScript);
		
		new File(outputHtml + ".md").delete();
		new File(scriptFilename).delete();

		RMarkdown2Step.deleteFolder(new File(folder));
		
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
		return new String[] { DockerPlugin.ID };
	}

}
