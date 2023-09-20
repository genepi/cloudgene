package cloudgene.mapred.jobs.steps;

import cloudgene.mapred.jobs.sdk.WorkflowContext;
import cloudgene.mapred.jobs.sdk.WorkflowStep;

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
