package cloudgene.mapred.jobs.steps;

import genepi.hadoop.common.WorkflowContext;
import genepi.hadoop.common.WorkflowStep;

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
