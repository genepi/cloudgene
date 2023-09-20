package cloudgene.mapred.steps;

import cloudgene.mapred.jobs.sdk.WorkflowContext;
import cloudgene.mapred.jobs.sdk.WorkflowStep;

public class ReturnTrueStep extends WorkflowStep {

	@Override
	public boolean run(WorkflowContext context) {

		try {
			Thread.sleep(500);
			return true;
			
		} catch (InterruptedException e) {
			e.printStackTrace();
			return true;
		}

	}

}
