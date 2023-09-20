package cloudgene.mapred.plugins.nextflow;

public class NextflowProcessConfig {

	private static final String DEFAULT_VIEW = "list";

	private String view = DEFAULT_VIEW;

	private String label = null;

	public String getView() {
		return view;
	}

	public void setView(String view) {
		this.view = view;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
