package cloudgene.mapred.steps;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.wdl.WdlStep;

import java.util.Map;

public class RMarkdownStep extends CloudgeneStep {

	@Override
	public boolean run(WdlStep step, CloudgeneContext context) {

		boolean useDocker = false;

		String dockerImage = RMarkdownDockerStep.DOCKER_R_BASE_IMAGE;

		Map<String, String> rscript = context.getSettings().getPlugin("rscript");
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

		if (useDocker) {
			RMarkdownDockerStep rMarkdownStep = new RMarkdownDockerStep();
			step.put("image", dockerImage);
			return rMarkdownStep.run(step, context);
		} else {
			RMarkdownLocalStep rMarkdownStep = new RMarkdownLocalStep();
			return rMarkdownStep.run(step, context);
		}

	}

	@Override
	public String[] getRequirements() {
		return new String[] {};
	}

}
