package cloudgene.mapred.steps;

import cloudgene.sdk.internal.WorkflowContext;
import cloudgene.sdk.internal.WorkflowStep;

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
