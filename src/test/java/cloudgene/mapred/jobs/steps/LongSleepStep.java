package cloudgene.mapred.jobs.steps;

import genepi.hadoop.common.WorkflowContext;
import genepi.hadoop.common.WorkflowStep;

public class LongSleepStep extends WorkflowStep {

	boolean killed = false;
	
	@Override
	public boolean run(WorkflowContext context) {

		try {
			for (int i = 0; i < 30 && !killed; i++) {
				Thread.sleep(1000);
				System.out.println("Sleep number " + i);
			}
			return true;

		} catch (InterruptedException e) {
			e.printStackTrace();
			return true;
		}

	}
	
	@Override
	public void kill() {
		killed = true;
	}

}
