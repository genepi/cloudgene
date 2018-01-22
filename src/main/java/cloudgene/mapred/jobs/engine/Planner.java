package cloudgene.mapred.jobs.engine;

import java.io.File;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.Environment;
import cloudgene.mapred.jobs.engine.graph.Graph;
import cloudgene.mapred.jobs.engine.graph.GraphEdge;
import cloudgene.mapred.jobs.engine.graph.GraphNode;
import cloudgene.mapred.jobs.engine.plugins.ParameterValueInput;
import cloudgene.mapred.jobs.engine.plugins.ParameterValueOutput;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlParameter;
import cloudgene.mapred.wdl.WdlParameterInput;
import cloudgene.mapred.wdl.WdlParameterOutput;
import cloudgene.mapred.wdl.WdlReader;
import cloudgene.mapred.wdl.WdlStep;
import cloudgene.mapred.wdl.WdlWorkflow;

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

	public Graph buildDAG(List<WdlStep> steps, WdlWorkflow config, CloudgeneContext context)
			throws MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException {

		Graph graph = new Graph(context);

		// build nodes
		GraphNode lastNode = null;
		for (WdlStep step : steps) {
			GraphNode node = new GraphNode(step, context);
			graph.addNode(node);
			if (lastNode != null) {
				graph.connect(lastNode, node, null);
			}
			lastNode = node;
		}

		// add output parameters
		for (GraphNode node : graph.getNodes()) {

			for (WdlParameter param : config.getOutputs()) {
				if (stepConsumesParameter(node.getStep(), param, context)
						&& !stepProducesParameter(node.getStep(), param, context)) {
					node.addInput(param.getId());
				}

				if (stepProducesParameter(node.getStep(), param, context)) {
					node.addOutput(param.getId());
				}
			}

			for (WdlParameter param : config.getInputs()) {
				if (stepConsumesParameter(node.getStep(), param, context)) {
					node.addInput(param.getId());
				}
			}
		}

		context.log("Planner: DAG created.");
		context.log("  Nodes: " + graph.getSize());
		for (GraphNode node : graph.getNodes()) {
			context.log("    " + node.getStep().getName());
			String inputs = "";
			for (String input : node.getInputs()) {
				inputs += input + " ";
			}
			context.log("      Inputs: " + inputs);
			String outputs = "";
			for (String output : node.getOutputs()) {
				outputs += output + " ";
			}
			context.log("      Outputs: " + outputs);
		}

		context.log("  Dipendencies: " + graph.getEdges().size());
		for (GraphEdge edge : graph.getEdges()) {
			context.log("    " + edge.getSource().getStep().getName() + "->" + edge.getTarget().getStep().getName());
		}

		return graph;

	}

	private boolean stepConsumesParameter(WdlStep step, WdlParameter param, CloudgeneContext context) {

		if (step.get("params") != null) {
			if (step.get("params").contains(context.get(param.getId()))) {
				return true;
			}
		}

		if (step.get("mapper") != null) {
			if (step.get("mapper").contains(context.get(param.getId()))) {
				return true;
			}
		}

		if (step.get("reducer") != null) {
			if (step.get("reducer").contains(context.get(param.getId()))) {
				return true;
			}
		}

		return false;

	}

	private boolean stepProducesParameter(WdlStep step, WdlParameter param, CloudgeneContext context) {
		if (step.getGenerates() != null) {
			return (step.getGenerates().contains(context.get(param.getId())));
		} else {
			return false;
		}
	}

}
