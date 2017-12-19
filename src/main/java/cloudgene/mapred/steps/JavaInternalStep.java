package cloudgene.mapred.steps;

import genepi.hadoop.common.WorkflowStep;
import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.wdl.WdlStep;

public class JavaInternalStep extends CloudgeneStep {

	private WorkflowStep workflowStep;

	public JavaInternalStep(WorkflowStep step) {
		this.workflowStep = step;
	}

	@Override
	public void setup(CloudgeneContext context) {
		workflowStep.setup(context);
	}

	@Override
	public void kill() {
		workflowStep.kill();
	}

	@Override
	public void updateProgress() {
		workflowStep.updateProgress();
	}

	@Override
	public boolean run(WdlStep step, CloudgeneContext context) {
		context.setConfig(step);
		return workflowStep.run(context);
	}

}
