package cloudgene.mapred.jobs.sdk;

import java.io.File;

public abstract class WorkflowStep {

	public String getFolder(Class clazz) {
		return new File(clazz.getProtectionDomain().getCodeSource()
				.getLocation().getPath()).getParent();
	}

	public void setup(WorkflowContext context) {

	}

	abstract public boolean run(WorkflowContext context);

	public int getMapProgress() {
		return 0;
	}

	public int getReduceProgress() {
		return 0;
	}

	public void updateProgress() {

	}

	public void kill() {

	}


}
