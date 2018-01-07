package cloudgene.mapred.jobs.engine;

import java.util.List;

import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.jobs.CloudgeneParameterOutput;
import cloudgene.mapred.jobs.engine.graph.Graph;
import cloudgene.mapred.jobs.engine.graph.GraphNode;

public class Executor {

	private GraphNode executableNode;

	public boolean execute(Graph graph) {

		graph.getContext().log("Executor: execute DAG...");

		while (graph.getSize() > 0) {
			List<GraphNode> nodes = graph.getSources();
			boolean successful = false;
			successful = executeNodesSequential(graph, nodes);
			if (!successful) {
				return false;
			}

		}

		return true;
	}

	private boolean executeNodesSequential(Graph graph, List<GraphNode> nodes) {

		for (GraphNode node : nodes) {

			executableNode = node;

			executableNode.run();
			// export results
			exportResults(graph, node);

			if (!executableNode.isSuccessful()) {
				return false;
			}
			// TODO: cache.addToCache(node, graph.getContext());

			graph.remove(node);
		}

		return true;
	}

	public void kill() {
		executableNode.kill();
	}

	public void updateProgress() {
		if (executableNode != null) {
			executableNode.updateProgress();
		}
	}

	public int getProgress() {
		if (executableNode != null) {
			return executableNode.getProgress();
		} else {
			return 0;
		}
	}

	private void exportResults(Graph graph, GraphNode node) {

		CloudgeneJob job = (CloudgeneJob) graph.getContext().getJob();

		for (CloudgeneParameterOutput out : job.getOutputParams()) {
			if (out.isAutoExport() && node.getOutputs().contains(out.getName())) {
				graph.getContext().println(
						"Export parameter '" + out.getName() + "'...");
				job.exportParameter(out);
			}
		}

	}

	public GraphNode getCurrentNode() {
		return executableNode;
	}

}
