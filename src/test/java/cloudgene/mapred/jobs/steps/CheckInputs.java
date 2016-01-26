package cloudgene.mapred.jobs.steps;

import genepi.hadoop.common.WorkflowContext;
import genepi.hadoop.common.WorkflowStep;
import genepi.io.FileUtil;

public class CheckInputs extends WorkflowStep {

	@Override
	public boolean run(WorkflowContext context) {

		String fileContent = FileUtil.readFileAsString(context.get("file"));

		String fileContentInFolder1 = FileUtil.readFileAsString(FileUtil.path(
				context.get("folder"), "test1.txt"));
		String fileContentInFolder2 = FileUtil.readFileAsString(FileUtil.path(
				context.get("folder"), "test2.txt"));

		boolean result = (context.get("text").equals("my-text"))
				&& (context.get("number").equals("27"))
				&& (context.get("checkbox").equals("valueFalse"))
				&& (context.get("list").equals("valuea"))
				&& fileContent.equals("content-of-my-file")
				&& fileContentInFolder1.equals("content-of-my-file-in-folder1")
				&& fileContentInFolder2.equals("content-of-my-file-in-folder2");

		return result;
	}

}
