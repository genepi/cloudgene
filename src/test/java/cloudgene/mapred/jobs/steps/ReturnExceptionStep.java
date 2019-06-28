package cloudgene.mapred.jobs.steps;

import cloudgene.sdk.internal.WorkflowContext;
import cloudgene.sdk.internal.WorkflowStep;

public class ReturnExceptionStep extends WorkflowStep {

	@Override
	public boolean run(WorkflowContext context) {

		try {
			Thread.sleep(500);
			throw new IndexOutOfBoundsException();
			
		} catch (InterruptedException e) {
			//e.printStackTrace();
			throw new IndexOutOfBoundsException();

		}

	}

}
