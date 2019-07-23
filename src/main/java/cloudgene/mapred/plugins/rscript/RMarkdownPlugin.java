package cloudgene.mapred.plugins.rscript;

import java.util.Map;

import cloudgene.mapred.plugins.IPlugin;
import cloudgene.mapred.plugins.docker.DockerBinary;
import cloudgene.mapred.steps.RMarkdownDockerStep;
import cloudgene.mapred.util.Settings;

public class RMarkdownPlugin implements IPlugin {

	public static final String ID = "rmarkdown";

	private boolean useDocker = true;

	private String dockerImage = RMarkdownDockerStep.DOCKER_R_BASE_IMAGE;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "RMarkdown";
	}

	@Override
	public boolean isInstalled() {
		if (useDocker) {
			return DockerBinary.isInstalled();
		} else {
			if (RScriptBinary.isInstalled()) {
				return RScriptBinary.isMarkdownInstalled();
			} else {
				return false;
			}
		}
	}

	@Override
	public String getDetails() {
		if (useDocker) {
			return "RMarkdown support enabled. Using docker image " + dockerImage;
		} else {
			return RScriptBinary.getMarkdownDetails();
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
			return "RMarkdown support enabled. Using docker image " + dockerImage;
		} else {
			if (RScriptBinary.isInstalled()) {
				if (isInstalled()) {
					return "RMarkdown support enabled.";
				} else {
					return "RMarkdown support disabled. Please install the following packages: "
							+ String.join(" ", RScriptBinary.PACKAGES) + "<br><br><pre>"
							+ RScriptBinary.getMarkdownErrorDetails() + "</pre>";
				}
			} else {
				return "RMarkdown support disabled. Please install or configure RScript.";
			}
		}
	}

}
