package cloudgene.sample;

import genepi.hadoop.common.WorkflowContext;
import genepi.hadoop.common.WorkflowStep;

public class SampleStep extends WorkflowStep {

	Thread t;

	@Override
	public boolean run(WorkflowContext context) {
		t = Thread.currentThread();
		try {
			Thread.sleep(20 * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("-------------------> KILL");
			return false;
		}

		System.out.println("-------------------> OKEY");

		return true;

	}

	@Override
	public void kill() {
		t.interrupt();

	}

}
