package cloudgene.mapred.server.responses;

import java.io.File;
import java.util.List;
import java.util.Vector;

import com.fasterxml.jackson.annotation.JsonInclude;

import cloudgene.mapred.jobs.Environment.Variable;
import cloudgene.mapred.util.Settings;
import genepi.io.FileUtil;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class NextflowConfigResponse {

	private String content = "";

	private List<Variable> variables = new Vector<Variable>();

	public static NextflowConfigResponse build(Settings settings) {
		NextflowConfigResponse response = new NextflowConfigResponse();
		String configFilename = settings.getNextflowConfig();
		File configFile = new File(configFilename);
		if (!configFile.exists()) {
			return response;
		}

		response.setContent(FileUtil.readFileAsString(configFilename));
		response.setVariables(settings.buildEnvironment().toList());
		return response;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	public void setVariables(List<Variable> variables) {
		this.variables = variables;
	}

	public List<Variable> getVariables() {
		return variables;
	}

}
