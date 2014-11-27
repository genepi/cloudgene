package cloudgene.mapred.steps;

import genepi.io.FileUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.wdl.WdlStep;

public class TemplateStep extends CloudgeneStep {

	@Override
	public void kill() {

	}

	@Override
	public boolean run(WdlStep step, CloudgeneContext context) {

		String templatePath = step.getTemplate();
		String outputPath = step.getOutput();

		String wd = context.getWorkingDirectory();

		File templateFile = new File(FileUtil.path(wd, templatePath));

		if (!templateFile.exists()) {
			context.error("Template " + templateFile.getAbsolutePath()
					+ " not found.");
			return false;
		}

		Velocity.setProperty("file.resource.loader.path", "/");
		VelocityContext context2 = new VelocityContext();

		// add input values to context
		for (String param : context.getInputs()) {
			context2.put(param, context.getInput(param));
		}

		// add output values to context
		for (String param : context.getOutputs()) {
			context2.put(param, context.getOutput(param));
		}

		// add mapping values to context
		for (String variable : step.getMapping().keySet()) {
			context2.put(variable, step.getMapping().get(variable));
		}

		try {

			StringWriter sw = new StringWriter();

			Template template = Velocity.getTemplate(templateFile
					.getAbsolutePath());
			template.merge(context2, sw);

			FileUtil.createDirectory(new File(outputPath).getParentFile().getAbsolutePath());
			
			FileUtil.writeStringBufferToFile(outputPath, sw.getBuffer());

			context.ok("Created file " + outputPath + ".");

			return true;

		} catch (Exception e) {
			context.error(e.getMessage());
			return false;
		}

	}
}
