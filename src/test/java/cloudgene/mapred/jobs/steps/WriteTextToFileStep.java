package cloudgene.mapred.jobs.steps;

import cloudgene.mapred.jobs.sdk.WorkflowContext;
import cloudgene.mapred.jobs.sdk.WorkflowStep;
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
