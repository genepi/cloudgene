package cloudgene.mapred.jobs.steps;

import cloudgene.mapred.jobs.sdk.WorkflowContext;
import cloudgene.mapred.jobs.sdk.WorkflowStep;

public class LongSleepStep extends WorkflowStep {

	boolean killed = false;
	
	@Override
	public boolean run(WorkflowContext context) {

		try {
			for (int i = 0; i < 50 && !killed; i++) {
				Thread.sleep(1000);
				System.out.println(context.getJobId() + ": Sleep number " + i);
			}
			return !killed;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}
	
	@Override
	public void kill() {
		killed = true;
	}

}
