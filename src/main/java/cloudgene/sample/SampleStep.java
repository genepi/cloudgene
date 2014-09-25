package cloudgene.sample;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.jobs.Message;
import cloudgene.mapred.wdl.WdlStep;

public class SampleStep extends CloudgeneStep {

	@Override
	public boolean run(WdlStep step, CloudgeneContext context) {
		
		message("HDFS File System check", Message.OK);
		message("Local File System check", Message.OK);
		
		message("Congratulations. Cloudgene works properly on your Hadoop Cluster!", Message.OK);
		
		return true;

	}

}
