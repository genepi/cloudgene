package cloudgene.mapred.jobs.steps;

import java.io.File;

import genepi.hadoop.common.WorkflowContext;
import genepi.hadoop.common.WorkflowStep;
import genepi.io.FileUtil;

public class CheckInputs extends WorkflowStep {

	@Override
	public boolean run(WorkflowContext context) {

		
		System.out.println("inpts: " + context.getInputs().toString());
		
		System.out.println((new File(context.get("file"))).exists());
		
		String fileContent = FileUtil.readFileAsString(context.get("file"));

		String fileContentInFolder1 = FileUtil.readFileAsString(FileUtil.path(
				context.get("folder"), "test1.txt"));
		String fileContentInFolder2 = FileUtil.readFileAsString(FileUtil.path(
				context.get("folder"), "test2.txt"));

		System.out.println("text: " + context.get("text"));
		System.out.println("number: " + context.get("number"));
		System.out.println("checkbox: " + context.get("checkbox"));
		System.out.println("list: " + context.get("list"));
		System.out.println("file: " + context.get("file") + " - " + fileContent);
		System.out.println("fileContentInFolder1: " +  context.get("folder") + " - " +fileContentInFolder1);
		System.out.println("fileContentInFolder1: " +  context.get("folder") + " - " +fileContentInFolder2);
		
		
		
		
		boolean result = (context.get("text").equals("my-text"))
				&& (context.get("number").equals("27"))
				&& (context.get("checkbox").equals("valueFalse"))
				&& (context.get("list").equals("keya"))
				&& fileContent.equals("content-of-my-file")
				&& fileContentInFolder1.equals("content-of-my-file-in-folder1")
				&& fileContentInFolder2.equals("content-of-my-file-in-folder2");

		return result;
	}

}
