package cloudgene.mapred.jobs.steps;

import cloudgene.sdk.internal.WorkflowContext;
import cloudgene.sdk.internal.WorkflowStep;
import genepi.io.FileUtil;

public class WriteTextToFileStep extends WorkflowStep {

	@Override
	public boolean run(WorkflowContext context) {

		String filename = context.getOutput("output");
		String text = context.getInput("inputtext");
		System.out.println("output: " + filename);
		try{
		FileUtil.writeStringBufferToFile(filename, new StringBuffer(text));
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
