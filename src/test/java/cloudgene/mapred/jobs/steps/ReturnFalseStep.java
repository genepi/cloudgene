package cloudgene.mapred.jobs.steps;

import cloudgene.mapred.jobs.sdk.WorkflowContext;
import cloudgene.mapred.jobs.sdk.WorkflowStep;

public class ReturnFalseStep extends WorkflowStep {

	@Override
	public boolean run(WorkflowContext context) {

		try {
			Thread.sleep(500);
			return false;
			
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}

	}

}
