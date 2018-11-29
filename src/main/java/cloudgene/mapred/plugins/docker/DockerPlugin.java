package cloudgene.mapred.plugins.docker;

import cloudgene.mapred.jobs.CloudgeneStepFactory;
import cloudgene.mapred.plugins.IPlugin;
import cloudgene.mapred.util.Settings;

public class DockerPlugin implements IPlugin {

	public static final String ID = "docker";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "Docker";
	}

	@Override
	public boolean isInstalled() {
		return DockerBinary.isInstalled();
	}

	@Override
	public String getDetails() {
		return DockerBinary.getVersion();
	}

	@Override
	public void configure(Settings settings) {
		CloudgeneStepFactory factory = CloudgeneStepFactory.getInstance();
		factory.register("docker", DockerStep.class);
	}

	@Override
	public String getStatus() {
		if (isInstalled()) {
			return "Docker support enabled.";
		} else {
			return "Docker Binary not found. Docker support disabled.";
		}
	}

}
