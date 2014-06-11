package cloudgene.mapred.jobs.engine.graph;

import java.util.List;
import java.util.Vector;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.wdl.WdlParameter;

public class Graph {

	private List<GraphNode> nodes;

	private List<GraphEdge> edges;

	private CloudgeneContext context;

	public Graph(CloudgeneContext context) {

		this.context = context;

		nodes = new Vector<GraphNode>();
		edges = new Vector<GraphEdge>();

	}

	public CloudgeneContext getContext() {
		return context;
	}

	public List<GraphNode> getNodes() {
		return nodes;
	}

	public List<GraphEdge> getEdges() {
		return edges;
	}

	public void connect(GraphNode source, GraphNode target, WdlParameter param) {
		GraphEdge edge = new GraphEdge(source, target, param);
		edges.add(edge);
	}

	public boolean areConnected(GraphNode source, GraphNode target) {
		for (GraphEdge edge : edges) {
			if (edge.getSource() == source && edge.getTarget() == target) {
				return true;
			}
		}
		return false;
	}

	public boolean remove(GraphNode node) {

		List<GraphEdge> remove = new Vector<GraphEdge>();

		nodes.remove(node);
		for (GraphEdge edge : edges) {
			if (edge.getSource() == node || edge.getTarget() == node) {
				remove.add(edge);
			}
		}
		edges.removeAll(remove);

		return true;
	}

	public int getInDegree(GraphNode node) {
		int degree = 0;

		for (GraphEdge edge : edges) {
			if (edge.getTarget() == node) {
				degree++;
			}
		}

		return degree;

	}

	public int getOutDegree(GraphNode node) {
		int degree = 0;

		for (GraphEdge edge : edges) {
			if (edge.getSource() == node) {
				degree++;
			}
		}

		return degree;
	}

	public List<GraphNode> getSources() {

		List<GraphNode> sources = new Vector<GraphNode>();

		for (GraphNode node : nodes) {
			if (getInDegree(node) == 0) {
				sources.add(node);
			}
		}

		return sources;

	}

	public List<GraphNode> getTargets() {

		List<GraphNode> targets = new Vector<GraphNode>();

		for (GraphNode node : nodes) {
			if (getOutDegree(node) == 0) {
				targets.add(node);
			}
		}

		return targets;
	}

	public int getSize() {
		return nodes.size();
	}

	public void addNode(GraphNode node) {
		nodes.add(node);
	}

}
