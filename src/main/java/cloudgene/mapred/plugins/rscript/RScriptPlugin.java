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

	private Settings settings;

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
			DockerBinary docker = DockerBinary.build(settings);
			return docker.isInstalled();
		} else {
			RScriptBinary rscript = RScriptBinary.build(settings);
			return rscript.isInstalled();
		}
	}

	@Override
	public String getDetails() {
		if (useDocker) {
			return "RScript support enabled. Using docker image " + dockerImage;
		} else {
			RScriptBinary rscript = RScriptBinary.build(settings);
			return rscript.getVersion();
		}
	}

	@Override
	public void configure(Settings settings) {
		this.settings = settings;
		Map<String, String> rscript = settings.getPlugin("rscript");
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
				return "RScript is configured to use Docker, but Docker is not installed.";
			}
		} else {
			if (isInstalled()) {
				return "RScript support enabled.";
			} else {
				return "RScript Binary not found. RScript support disabled.";
			}
		}
	}
}
