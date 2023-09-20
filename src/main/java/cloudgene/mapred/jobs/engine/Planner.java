package cloudgene.mapred.jobs.engine;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import cloudgene.mapred.jobs.CloudgeneContext;
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

		context2.putAll(settings.buildEnvironment().addApplication(app).addContext(context).toMap());

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
