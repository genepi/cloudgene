package cloudgene.mapred;

import cloudgene.mapred.jobs.engine.graph.Graph;
import cloudgene.mapred.jobs.engine.graph.GraphNode;

public class GraphVizTool {

	public static void printDot(Graph graph) {

		/*System.out.println("digraph cloudgene {");

		// nodes

		System.out.println("node [shape=circle]");
		for (GraphNode node : graph.getNodes()) {
			System.out.println(node.getStep().getName() + ";");
		}

		System.out.println("node [shape=box]");
		for (GraphEdge edge : graph.getEdges()) {
			System.out.println(edge.getParameter().getId() + ";");
		}

		// edges
		for (GraphEdge edge : graph.getEdges()) {

			System.out.println(edge.getSource().getStep().getName() + "->"
					+ edge.getParameter().getId());

			System.out.println(edge.getParameter().getId() + "->"
					+ edge.getTarget().getStep().getName());

		}

		System.out.println("}");*/
	}

	public static void printDot2(Graph graph) {

		System.out.println("digraph cloudgene {");

		// nodes

		System.out.println("node [shape=box]");
		for (GraphNode node : graph.getNodes()) {
			System.out.println(node.getStep().getName() + ";");
		}

		// edges
	/*	for (GraphEdge edge : graph.getEdges()) {

			System.out.println(edge.getSource().getStep().getName() + "->"
					+ edge.getTarget().getStep().getName());


		}*/

		System.out.println("}");

	}

}
