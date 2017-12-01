package cloudgene.mapred.wdl;

import java.util.List;
import java.util.Vector;

public class WdlWorkflow {

	private String jar;

	private String mapper;

	private String reducer;

	private String exec;

	private String pig;

	private String params;

	private List<WdlStep> steps = new Vector<WdlStep>();

	private List<WdlParameter> inputs = new Vector<WdlParameter>();

	private List<WdlParameter> outputs = new Vector<WdlParameter>();

	private String path;

	private String manifestFile;

	private String type = "sequence";

	private WdlStep setup = null;

	private List<WdlStep> setups = new Vector<WdlStep>();

	private WdlStep onFailure = null;

	public String getJar() {
		return jar;
	}

	public void setJar(String jar) {
		this.jar = jar;
	}

	public String getMapper() {
		return mapper;
	}

	public void setMapper(String mapper) {
		this.mapper = mapper;
	}

	public String getReducer() {
		return reducer;
	}

	public void setReducer(String reducer) {
		this.reducer = reducer;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public List<WdlParameter> getInputs() {
		return inputs;
	}

	public void setInputs(List<WdlParameter> inputs) {
		this.inputs = inputs;
	}

	public List<WdlParameter> getOutputs() {
		return outputs;
	}

	public void setOutputs(List<WdlParameter> outputs) {
		this.outputs = outputs;
	}

	public List<WdlStep> getSteps() {
		return steps;
	}

	public void setSteps(List<WdlStep> steps) {
		this.steps = steps;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setExec(String exec) {
		this.exec = exec;
	}

	public String getExec() {
		return exec;
	}

	public void setPig(String pig) {
		this.pig = pig;
	}

	public String getPig() {
		return pig;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setManifestFile(String manifestFile) {
		this.manifestFile = manifestFile;
	}

	public String getManifestFile() {
		return manifestFile;
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
		for (WdlParameter output : outputs) {
			if (output.isHdfs()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasHdfsInputs() {
		for (WdlParameter input : inputs) {
			if (input.isHdfs()) {
				return true;
			}
		}
		return false;
	}

}
