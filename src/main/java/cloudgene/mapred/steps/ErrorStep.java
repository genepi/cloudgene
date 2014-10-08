package cloudgene.mapred.steps;

import genepi.hadoop.common.WorkflowContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.wdl.WdlStep;

public class ErrorStep extends CloudgeneStep{

	private String errorMessage;
	
	public ErrorStep(String errorMessage){
		this.errorMessage = errorMessage;
	}
	
	@Override
	public boolean run(WdlStep step, WorkflowContext context) {
		context.error(errorMessage);
		return false;
	}

}
