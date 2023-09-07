package cloudgene.mapred.jobs.engine;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.Environment;
import cloudgene.mapred.jobs.engine.plugins.ParameterValueInput;
import cloudgene.mapred.jobs.engine.plugins.ParameterValueOutput;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlParameterInput;
import cloudgene.mapred.wdl.WdlParameterOutput;
import cloudgene.mapred.wdl.WdlReader;
import groovy.text.SimpleTemplateEngine;

public class Planner {

	public WdlApp evaluateWDL(WdlApp app, CloudgeneContext context, Settings settings) throws Exception {

		Map<String, String> context2 = new HashMap<String, String>();

		// add input values to context
		for (WdlParameterInput param : app.getWorkflow().getInputs()) {
			context2.put(param.getId(), context.getInput(param.getId()));
		}

		// add output values to context
		for (WdlParameterOutput param : app.getWorkflow().getOutputs()) {
			context2.put(param.getId(), context.getOutput(param.getId()));
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

		// add user specific variables
		context2.put("user_email", context.getUser().getMail());
		context2.put("user_name", context.getUser().getUsername());
		context2.put("user_full_name", context.getUser().getFullName());

		File manifest = new File(app.getManifestFile());

		SimpleTemplateEngine engine = new SimpleTemplateEngine();
		String content = engine.createTemplate(manifest).make(context2).toString();

		WdlApp app2 = WdlReader.loadAppFromString(manifest.getAbsolutePath(), content);

		app2.getWorkflow().setInputs(app.getWorkflow().getInputs());
		app2.getWorkflow().setOutputs(app.getWorkflow().getOutputs());

		context.log("Planner: WDL evaluated.");

		return app2;
	}

}
