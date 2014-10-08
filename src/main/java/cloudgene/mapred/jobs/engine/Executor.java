package cloudgene.mapred.jobs.engine;

import java.util.List;
import java.util.Vector;

import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.jobs.CloudgeneParameter;
import cloudgene.mapred.jobs.cache.CacheDirectory;
import cloudgene.mapred.jobs.engine.graph.Graph;
import cloudgene.mapred.jobs.engine.graph.GraphNode;
import cloudgene.mapred.util.ParallelCalculation;
import cloudgene.mapred.wdl.WdlParameter;

public class Executor {

	private CacheDirectory cache;

	private GraphNode executableNode;

	private boolean useDag = false;

	public Executor(CacheDirectory cache) {
		this.cache = cache;
	}

	public boolean execute(Graph graph) {

		graph.getContext().log("Executor: execute DAG...");

		while (graph.getSize() > 0) {
			List<GraphNode> nodes = graph.getSources();
			boolean successful = false;
			if (useDag) {
				successful = executeNodesParallel(graph, nodes);
			} else {
				successful = executeNodesSequential(graph, nodes);
			}
			if (!successful) {
				return false;
			}

		}

		return true;
	}

	private boolean executeNodesParallel(Graph graph, List<GraphNode> nodes) {

		ParallelCalculation parallelCalculation = new ParallelCalculation();
		parallelCalculation.setThreads(10);
		List<Thread> threads = new Vector<Thread>();
		for (GraphNode node : nodes) {
			// TODO: implement kill in ParallelCalculation class
			executableNode = node;
			threads.add(new Thread(executableNode));
			graph.remove(node);
		}

		if (!parallelCalculation.run(threads)) {
			for (GraphNode node : nodes) {
				// export results
				exportResults(graph, node);
			}
			return false;
		} else {
			for (GraphNode node : nodes) {
				// TODO: cache.addToCache(node, graph.getContext());
				// export results
				exportResults(graph, node);
			}
			return true;
		}

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

	public int getMapProgress() {
		if (executableNode != null) {
			return executableNode.getMapProgress();
		} else {
			return 0;
		}
	}

	public int getReduceProgress() {
		if (executableNode != null) {
			return executableNode.getReduceProgress();
		} else {
			return 0;
		}
	}

	public void setUseDag(boolean useDag) {
		this.useDag = useDag;
	}

	private void exportResults(Graph graph, GraphNode node) {

		CloudgeneJob job = (CloudgeneJob) graph.getContext().getJob();

		for (CloudgeneParameter out : job.getOutputParams()) {
			if (out.isAutoExport() && node.getOutputs().contains(out.getName())) {
				graph.getContext().println(
						"Export parameter '" + out.getName() + "'...");
				job.exportParameter(out);
			}
		}

	}

}
