package cloudgene.mapred.steps;

import genepi.hadoop.common.WorkflowStep;
import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.wdl.WdlStep;

public class ExternStep extends CloudgeneStep {

	private WorkflowStep workflowStep;

	public ExternStep(WorkflowStep step) {
		this.workflowStep = step;
	}

	@Override
	public int getReduceProgress() {
		return workflowStep.getReduceProgress();
	}

	@Override
	public int getMapProgress() {
		return workflowStep.getMapProgress();
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
		context.setConfig(step.getConfig());
		return workflowStep.run(context);
	}

}
