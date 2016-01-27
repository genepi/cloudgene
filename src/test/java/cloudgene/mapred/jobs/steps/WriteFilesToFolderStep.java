package cloudgene.mapred.jobs.steps;

import genepi.hadoop.common.WorkflowContext;
import genepi.hadoop.common.WorkflowStep;
import genepi.io.FileUtil;

public class WriteFilesToFolderStep extends WorkflowStep {

	@Override
	public boolean run(WorkflowContext context) {

		String output = context.getOutput("output");

		String text = context.getInput("inputtext");

		try {
			FileUtil.writeStringBufferToFile(
					FileUtil.path(output, "file1.txt"), new StringBuffer(text));
			FileUtil.writeStringBufferToFile(
					FileUtil.path(output, "file2.txt"), new StringBuffer(text));
			FileUtil.writeStringBufferToFile(
					FileUtil.path(output, "file3.txt"), new StringBuffer(text));
			FileUtil.writeStringBufferToFile(
					FileUtil.path(output, "file4.txt"), new StringBuffer(text));
			FileUtil.writeStringBufferToFile(
					FileUtil.path(output, "file5.txt"), new StringBuffer(text));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

}
