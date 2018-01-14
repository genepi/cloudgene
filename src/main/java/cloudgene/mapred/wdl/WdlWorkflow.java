package cloudgene.mapred.wdl;

import java.util.List;
import java.util.Vector;

public class WdlWorkflow {

	private List<WdlStep> steps = new Vector<WdlStep>();

	private List<WdlParameterInput> inputs = new Vector<WdlParameterInput>();

	private List<WdlParameterOutput> outputs = new Vector<WdlParameterOutput>();

	private String type = "sequence";

	private WdlStep setup = null;

	private List<WdlStep> setups = new Vector<WdlStep>();

	private WdlStep onFailure = null;

	public List<WdlParameterInput> getInputs() {
		return inputs;
	}

	public void setInputs(List<WdlParameterInput> inputs) {
		this.inputs = inputs;
	}

	public List<WdlParameterOutput> getOutputs() {
		return outputs;
	}

	public void setOutputs(List<WdlParameterOutput> outputs) {
		this.outputs = outputs;
	}

	public List<WdlStep> getSteps() {
		return steps;
	}

	public void setSteps(List<WdlStep> steps) {
		this.steps = steps;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setSetup(WdlStep setup) {
		this.setup = setup;
	}

	public WdlStep getSetup() {
		return setup;
	}

	public void setSetups(List<WdlStep> setups) {
		this.setups = setups;
	}

	public List<WdlStep> getSetups() {
		return setups;
	}

	public void setOnFailure(WdlStep onFailure) {
		this.onFailure = onFailure;
	}

	public WdlStep getOnFailure() {
		return onFailure;
	}

	public boolean hasHdfsOutputs() {
		for (WdlParameterOutput output : outputs) {
			if (output.isHdfs()) {
				return true;
			}
		}
		return false;
	}

	public boolean hasHdfsInputs() {
		for (WdlParameterInput input : inputs) {
			if (input.isHdfs()) {
				return true;
			}
		}
		return false;
	}

}
