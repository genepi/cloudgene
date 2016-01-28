package cloudgene.mapred.jobs.steps;

import genepi.hadoop.common.WorkflowContext;
import genepi.hadoop.common.WorkflowStep;

public class ThreeTasksStep extends WorkflowStep {

	@Override
	public boolean run(WorkflowContext context) {

		context.beginTask("cloudgene-task1");
		context.endTask("cloudgene-task1", WorkflowContext.OK);

		context.beginTask("cloudgene-task2");
		context.endTask("cloudgene-task2", WorkflowContext.OK);

		context.beginTask("cloudgene-task3");
		context.endTask("cloudgene-task3", WorkflowContext.OK);

		return true;

	}

}
