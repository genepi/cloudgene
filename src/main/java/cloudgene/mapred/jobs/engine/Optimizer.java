package cloudgene.mapred.jobs.engine;

import java.net.MalformedURLException;
import java.util.List;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.cache.CacheDirectory;
import cloudgene.mapred.jobs.engine.graph.Graph;
import cloudgene.mapred.jobs.engine.graph.GraphEdge;
import cloudgene.mapred.jobs.engine.graph.GraphNode;

public class Optimizer {

	private CacheDirectory cache;

	public Optimizer(CacheDirectory cache) {
		this.cache = cache;
	}

	public void optimize(Graph graph) throws MalformedURLException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException {

		boolean optimization = true;
		int noOptimizations = 0;
		while (optimization) {
			List<GraphNode> nodes = graph.getSources();
			optimization = false;
			for (GraphNode node : nodes) {
				if (cache.isCached(node, graph.getContext())) {
					graph.remove(node);
					cache.restore(node, graph.getContext());
					noOptimizations++;
					optimization = true;
				}
			}
		}

		CloudgeneContext context = graph.getContext();

		context.log("Optimizer: DAG optimized.");
		context.log("  Nodes: " + graph.getSize());
		context.log("  Nodes removed: " + noOptimizations);
		context.log("  Dipendencies: " + graph.getEdges().size());
		for (GraphEdge edge : graph.getEdges()) {
			context.log("    " + edge.getSource().getStep().getName() + "->"
					+ edge.getTarget().getStep().getName());
		}
	}

}
