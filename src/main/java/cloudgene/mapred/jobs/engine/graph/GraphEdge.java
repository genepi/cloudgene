package cloudgene.mapred.jobs.engine.graph;

import cloudgene.mapred.wdl.WdlParameter;

public class GraphEdge {

	private WdlParameter parameter;

	private GraphNode source;

	private GraphNode target;

	public GraphEdge(GraphNode source, GraphNode target, WdlParameter parameter) {
		this.source = source;
		this.target = target;
		this.parameter = parameter;
	}

	public WdlParameter getParameter() {
		return parameter;
	}

	public void setParameter(WdlParameter parameter) {
		this.parameter = parameter;
	}

	public GraphNode getSource() {
		return source;
	}

	public void setSource(GraphNode source) {
		this.source = source;
	}

	public GraphNode getTarget() {
		return target;
	}

	public void setTarget(GraphNode target) {
		this.target = target;
	}

}
