package cloudgene.mapred.jobs.steps;

import genepi.hadoop.common.WorkflowContext;
import genepi.hadoop.common.WorkflowStep;

public class WriteTextToStdOutStep extends WorkflowStep {

	@Override
	public boolean run(WorkflowContext context) {

		context.println("taks write to system out");
		context.println("taks write to system out2");
		context.println("taks write to system out3");

		context.log("taks write to log");
		context.log("taks write to log2");
		context.log("taks write to log3");
		
		return true;

	}

}
