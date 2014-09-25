package cloudgene.sample;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.jobs.Message;
import cloudgene.mapred.wdl.WdlStep;

public class SampleStep extends CloudgeneStep {

	@Override
	public boolean run(WdlStep step, CloudgeneContext context) {

		StringBuilder output = new StringBuilder();
		output.append("checkbox: " + context.get("checkbox") +"\n");
		output.append("hdfs-folder: " + context.get("folder")+"\n");
		output.append("number: " + context.get("number")+"\n");
		output.append("text: " + context.get("text")+"\n");
		output.append("list: " + context.get("list")+"\n");
		
		message(output.toString(), Message.OK);

		return true;

	}

}
