package cloudgene.mapred.steps;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.wdl.WdlStep;
import genepi.io.FileUtil;

public class LinkTestStep extends CloudgeneStep {

	@Override
	public boolean run(WdlStep step, CloudgeneContext context) {
		
		context.setConfig(step);
		
		String output = context.get("output");
		FileUtil.writeStringBufferToFile(FileUtil.path(output, "lukas.txt"), new StringBuffer("lukas"));
		
		String link = context.createLinkToFile("output", "lukas.txt");
		
		context.ok("Click here: "  + link);
		
		return true;

	}

}
