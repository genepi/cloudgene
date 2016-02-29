package cloudgene.mapred.jobs.steps;

import genepi.hadoop.common.WorkflowContext;
import genepi.hadoop.common.WorkflowStep;

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
