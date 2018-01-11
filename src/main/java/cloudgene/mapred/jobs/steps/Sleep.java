package cloudgene.mapred.jobs.steps;

import genepi.hadoop.common.WorkflowContext;
import genepi.hadoop.common.WorkflowStep;

public class Sleep extends WorkflowStep {

	boolean killed = false;
	
	@Override
	public boolean run(WorkflowContext context) {

		context.beginTask("...");
		try{
			//throw new NullPointerException();
			Thread.sleep(30000);
		}catch(Exception e){
		//context.updateTask(e.getMessage(), WorkflowContext.OK);
		}
		return true;
	}
	
	@Override
	public void kill() {
		killed = true;
	}

}
