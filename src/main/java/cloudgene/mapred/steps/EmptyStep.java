package cloudgene.mapred.steps;

import genepi.hadoop.common.WorkflowContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.wdl.WdlStep;

public class EmptyStep extends CloudgeneStep{

	@Override
	public boolean run(WdlStep step, WorkflowContext context) {
		return false;
	}

}
