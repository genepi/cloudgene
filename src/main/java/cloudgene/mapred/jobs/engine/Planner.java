package cloudgene.mapred.jobs.engine;

import java.io.File;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.engine.graph.Graph;
import cloudgene.mapred.jobs.engine.graph.GraphEdge;
import cloudgene.mapred.jobs.engine.graph.GraphNode;
import cloudgene.mapred.jobs.engine.plugins.ParameterValue;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlMapReduce;
import cloudgene.mapred.wdl.WdlParameter;
import cloudgene.mapred.wdl.WdlReader;
import cloudgene.mapred.wdl.WdlStep;

public class Planner {

	public WdlApp evaluateWDL(WdlMapReduce config, CloudgeneContext context) throws Exception {

		Velocity.setProperty("file.resource.loader.path", "/");
		VelocityContext context2 = new VelocityContext();

		// add input values to context
		for (WdlParameter param : config.getInputs()) {
			context2.put(param.getId(), new ParameterValue(param, context.getInput(param.getId())));
		}

		// add output values to context
		for (WdlParameter param : config.getOutputs()) {
			context2.put(param.getId(), new ParameterValue(param, context.getOutput(param.getId())));
		}

		context2.put("workdir", new File(context.getWorkingDirectory()).getAbsolutePath());

		File manifest = new File(config.getManifestFile());

		StringWriter sw = null;
		try {

			Template template = Velocity.getTemplate(manifest.getAbsolutePath());
			sw = new StringWriter();
			template.merge(context2, sw);

		} catch (Exception e) {
			throw e;
		}

		WdlApp app = WdlReader.loadAppFromString(manifest.getAbsolutePath(), sw.toString());

		app.getMapred().setInputs(config.getInputs());
		app.getMapred().setOutputs(config.getOutputs());

		context.println("Planner: WDL evaluated.");

		return app;
	}

	public Graph buildDAG(List<WdlStep> steps, WdlMapReduce config, CloudgeneContext context)
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

		if (step.getParams() != null) {
			if (step.getParams().contains(context.get(param.getId()))) {
				return true;
			}
		}

		if (step.getMapper() != null) {
			if (step.getMapper().contains(context.get(param.getId()))) {
				return true;
			}
		}

		if (step.getReducer() != null) {
			if (step.getReducer().contains(context.get(param.getId()))) {
				return true;
			}
		}

		return false;

	}

	private boolean stepProducesParameter(WdlStep step, WdlParameter param, CloudgeneContext context) {

		return (step.getGenerates().contains(context.get(param.getId())));

	}

}
