package cloudgene.mapred.steps;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.jobs.sdk.WorkflowStep;
import cloudgene.mapred.wdl.WdlStep;

public class JavaInternalStep extends CloudgeneStep {

	private WorkflowStep workflowStep;

	public JavaInternalStep(WorkflowStep step) {
		this.workflowStep = step;
	}

	@Override
	public void setup(CloudgeneContext context) {
		super.setup(context);
		workflowStep.setup(context);
	}

	@Override
	public void kill() {
		super.kill();
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
