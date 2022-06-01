package cloudgene.mapred.steps;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.jobs.Message;
import cloudgene.mapred.wdl.WdlStep;
import genepi.io.FileUtil;
import genepi.io.text.LineWriter;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;

public class HtmlWidgetStep extends CloudgeneStep {

	@Override
	public boolean run(WdlStep step, CloudgeneContext context) {

		String workingDirectory = context.getWorkingDirectory();

		String template = step.get("template");
		if (template == null || template.isEmpty()) {
			context.endTask("Execution failed. Please set the 'template' parameter.", Message.ERROR);
			return false;
		}

		File templateFile = new File(FileUtil.path(workingDirectory, template));

		if (!templateFile.exists()) {
			context.error("Template " + templateFile.getAbsolutePath() + " not found.");
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

		// add step values to context
		for (String variable : step.keySet()) {
			String value = step.get(variable);
			if (value.endsWith(".json")) {
				// replace json files with content.
				String jsonFilename = "";
				if (value.startsWith("/")) {
					jsonFilename = value;
				} else {
					jsonFilename = FileUtil.path(workingDirectory, value);
				}

				if (new File(jsonFilename).exists()) {
					value = FileUtil.readFileAsString(jsonFilename);
				}else {
					context.log("Warning! Json File '" + jsonFilename + "' not found. Replaced " + variable + " with empty string.");
					value = "";
				}
			}
			context2.put(variable, value);
		}

		try {

			StringWriter sw = new StringWriter();

			Template velocityTemplate = Velocity.getTemplate(templateFile.getAbsolutePath());
			velocityTemplate.merge(context2, sw);

			// create html page with embeeded css and js

			String htmlFile = FileUtil.path(context.getLocalTemp(), "html_widget.html");

			LineWriter writer = new LineWriter(htmlFile);
			writer.write("<!DOCTYPE html>");
			writer.write("<html>");
			writer.write("<head>");
			writer.write("<meta charset=\"utf-8\">");
			writer.write("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
			if (step.get("stylesheet") != null) {
				for (String css : step.get("stylesheet").split(",")) {
					css = css.trim();
					String data = "";
					if (!css.startsWith("http://") && !css.startsWith("https://")) {
						String content = encode(FileUtil.path(workingDirectory, css));
						data = "data:text/css;base64," + content;
					} else {
						data = css;
					}
					writer.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + data + "\"/>");
				}
			}
			if (step.get("scripts") != null) {
				for (String script : step.get("scripts").split(",")) {
					script = script.trim();
					String data = "";
					if (!script.startsWith("http://") && !script.startsWith("https://")) {
						String content = encode(FileUtil.path(workingDirectory, script));
						data = "data:application/x-javascript/css;base64," + content;
					} else {
						data = script;
					}
					writer.write("<script src=\"" + data + "\"></script>");
				}
			}
			writer.write("</head>");
			writer.write("<body>");
			writer.write(sw.getBuffer().toString());

			writer.write("  </body>");
			writer.write("</html>");
			writer.close();

			String output = step.get("output");
			
			if (output == null || output.isEmpty()) {
				context.addFile(htmlFile);	
			}else {
				File file = new File(output);
				new File(htmlFile).renameTo(file);

				//copy assets folder if set
				String assets = step.get("assets");
				if (assets != null && !assets.isEmpty()) {
					File parent = file.getParentFile();
					String assetsSource = FileUtil.path(workingDirectory, assets);
					String assetsTarget = FileUtil.path(parent.getAbsolutePath(), assets);
					FileUtil.createDirectory(assetsTarget);
					FileUtil.copyDirectory(assetsSource, assetsTarget);
				}
				
				context.ok("Html report created.");
			}
			
			return true;

		} catch (Exception e) {
			context.error(e.getMessage());
			return false;
		}

	}

	private static String encode(String sourceFile) throws Exception {
		return java.util.Base64.getEncoder().encodeToString(loadFileAsBytesArray(sourceFile));
	}

	public static byte[] loadFileAsBytesArray(String fileName) throws Exception {

		File file = new File(fileName);
		int length = (int) file.length();
		BufferedInputStream reader = new BufferedInputStream(new FileInputStream(file));
		byte[] bytes = new byte[length];
		reader.read(bytes, 0, length);
		reader.close();
		return bytes;

	}
}
