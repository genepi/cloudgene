package cloudgene.mapred.jobs.steps;

import cloudgene.mapred.jobs.sdk.WorkflowContext;
import cloudgene.mapred.jobs.sdk.WorkflowStep;

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
