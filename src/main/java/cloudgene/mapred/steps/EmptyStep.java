package cloudgene.mapred.steps;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.wdl.WdlStep;

public class EmptyStep extends CloudgeneStep{

	@Override
	public boolean run(WdlStep step, CloudgeneContext context) {
		return false;
	}

}
