package cloudgene.mapred.plugins.nextflow;

import cloudgene.mapred.jobs.CloudgeneStepFactory;
import cloudgene.mapred.plugins.IPlugin;
import cloudgene.mapred.util.Settings;

public class NextflowPlugin implements IPlugin {

	public static final String ID = "nextflow";

	private Settings settings;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "Nextflow";
	}

	@Override
	public boolean isInstalled() {
		NextflowBinary binary = NextflowBinary.build(settings);
		return binary.isInstalled();
	}

	@Override
	public String getDetails() {
		NextflowBinary binary = NextflowBinary.build(settings);
		return binary.getVersion();
	}

	@Override
	public void configure(Settings settings) {
		this.settings = settings;
		CloudgeneStepFactory factory = CloudgeneStepFactory.getInstance();
		factory.register("nextflow", NextflowStep.class);
	}

	@Override
	public String getStatus() {
		if (isInstalled()) {
			return "Nextflow support enabled.";
		} else {
			return "Nextflow Binary not found. Nextflow support disabled.";
		}
	}

}
