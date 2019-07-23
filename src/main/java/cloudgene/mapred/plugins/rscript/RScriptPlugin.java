package cloudgene.mapred.plugins.rscript;

import java.util.Map;

import cloudgene.mapred.plugins.IPlugin;
import cloudgene.mapred.plugins.docker.DockerBinary;
import cloudgene.mapred.steps.RMarkdownDockerStep;
import cloudgene.mapred.util.Settings;

public class RScriptPlugin implements IPlugin {

	public static final String ID = "rscript";

	private boolean useDocker = true;

	private String dockerImage = RMarkdownDockerStep.DOCKER_R_BASE_IMAGE;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "RScript";
	}

	@Override
	public boolean isInstalled() {
		if (useDocker) {
			return DockerBinary.isInstalled();
		} else {
			return RScriptBinary.isInstalled();
		}
	}

	@Override
	public String getDetails() {
		if (useDocker) {
			return "RScript support enabled. Using docker image " + dockerImage;
		}else {
			return RScriptBinary.getVersion();
		}
	}

	@Override
	public void configure(Settings settings) {
		Map<String, String> rscript = settings.getRscript();
		if (rscript != null) {
			String useDockerString = rscript.get("docker");
			if (useDockerString != null) {
				useDocker = (useDockerString.equals("true"));
			}
			String dockerImageString = rscript.get("image");
			if (dockerImageString != null) {
				dockerImage = dockerImageString;
			}

		}
	}

	@Override
	public String getStatus() {
		if (useDocker) {
			if (isInstalled()) {
				return "RScript support enabled. Using docker image " + dockerImage;
			} else {
				return "RScript is configured to use Docker. Docker is not installed.";
			}
		} else {
			if (isInstalled()) {
				return "RScript support enabled.";
			} else {
				return "RScript Binary not found. Please check if R is installed and file " + RScriptBinary.RSCRIPT_PATH
						+ " exists.";
			}
		}
	}
}
