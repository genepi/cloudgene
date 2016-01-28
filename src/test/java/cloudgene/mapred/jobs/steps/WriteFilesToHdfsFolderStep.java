package cloudgene.mapred.jobs.steps;

import genepi.hadoop.HdfsUtil;
import genepi.hadoop.common.WorkflowContext;
import genepi.hadoop.common.WorkflowStep;
import genepi.io.FileUtil;

public class WriteFilesToHdfsFolderStep extends WorkflowStep {

	@Override
	public boolean run(WorkflowContext context) {

		String output = context.getOutput("output");
		String temp = context.getLocalTemp();

		String text = context.getInput("inputtext");

		try {
			FileUtil.writeStringBufferToFile(FileUtil.path(temp, "file1.txt"),
					new StringBuffer(text));
			HdfsUtil.put(FileUtil.path(temp, "file1.txt"),
					HdfsUtil.path(output, "file1.txt"));

			FileUtil.writeStringBufferToFile(FileUtil.path(temp, "file2.txt"),
					new StringBuffer(text));
			HdfsUtil.put(FileUtil.path(temp, "file2.txt"),
					HdfsUtil.path(output, "file2.txt"));

			FileUtil.writeStringBufferToFile(FileUtil.path(temp, "file3.txt"),
					new StringBuffer(text));
			HdfsUtil.put(FileUtil.path(temp, "file3.txt"),
					HdfsUtil.path(output, "file3.txt"));

			FileUtil.writeStringBufferToFile(FileUtil.path(temp, "file4.txt"),
					new StringBuffer(text));
			HdfsUtil.put(FileUtil.path(temp, "file4.txt"),
					HdfsUtil.path(output, "file4.txt"));

			FileUtil.writeStringBufferToFile(FileUtil.path(temp, "file5.txt"),
					new StringBuffer(text));
			HdfsUtil.put(FileUtil.path(temp, "file5.txt"),
					HdfsUtil.path(output, "file5.txt"));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

}
