package cloudgene.mapred.jobs.engine;

import java.io.File;
import java.io.StringWriter;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.Environment;
import cloudgene.mapred.jobs.engine.plugins.ParameterValueInput;
import cloudgene.mapred.jobs.engine.plugins.ParameterValueOutput;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlParameterInput;
import cloudgene.mapred.wdl.WdlParameterOutput;
import cloudgene.mapred.wdl.WdlReader;

public class Planner {

	public WdlApp evaluateWDL(WdlApp app, CloudgeneContext context, Settings settings) throws Exception {

		Velocity.setProperty("file.resource.loader.path", "/");
		VelocityContext context2 = new VelocityContext();

		// add input values to context
		for (WdlParameterInput param : app.getWorkflow().getInputs()) {
			context2.put(param.getId(), new ParameterValueInput(param, context.getInput(param.getId())));
		}

		// add output values to context
		for (WdlParameterOutput param : app.getWorkflow().getOutputs()) {
			context2.put(param.getId(), new ParameterValueOutput(param, context.getOutput(param.getId())));
		}

		// add job variables
		Map<String, String> envJob = Environment.getJobVariables(context);
		for (String key : envJob.keySet()) {
			context2.put(key, envJob.get(key));
		}

		// add app variables
		Map<String, String> envApp = Environment.getApplicationVariables(app, settings);
		for (String key : envApp.keySet()) {
			context2.put(key, envApp.get(key));
		}

		File manifest = new File(app.getManifestFile());

		StringWriter sw = null;
		try {

			Template template = Velocity.getTemplate(manifest.getAbsolutePath());
			sw = new StringWriter();
			template.merge(context2, sw);

		} catch (Exception e) {
			throw e;
		}

		WdlApp app2 = WdlReader.loadAppFromString(manifest.getAbsolutePath(), sw.toString());

		app2.getWorkflow().setInputs(app.getWorkflow().getInputs());
		app2.getWorkflow().setOutputs(app.getWorkflow().getOutputs());

		context.log("Planner: WDL evaluated.");

		return app2;
	}

}
