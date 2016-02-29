package cloudgene.mapred.jobs.steps;

import genepi.hadoop.HdfsUtil;
import genepi.hadoop.common.WorkflowContext;
import genepi.hadoop.common.WorkflowStep;
import genepi.io.FileUtil;

public class WriteTextToHdfsFileStep extends WorkflowStep {

	@Override
	public boolean run(WorkflowContext context) {

		String temp = context.getLocalTemp();
		String hdfsFilename = context.getOutput("output");
		String text = context.getInput("inputtext");

		try {
			FileUtil.writeStringBufferToFile(FileUtil.path(temp, "temp.txt"),
					new StringBuffer(text));
			HdfsUtil.put(FileUtil.path(temp, "temp.txt"), hdfsFilename);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
