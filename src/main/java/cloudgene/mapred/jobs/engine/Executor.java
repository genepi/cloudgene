package cloudgene.mapred.jobs.engine;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.wdl.WdlStep;

public class Executor {

	private ExecutableStep executableNode;

	private static Logger log = LoggerFactory.getLogger(Executor.class);

	public boolean execute(List<WdlStep> steps, CloudgeneContext context) throws Exception {

		context.log("Execute " + steps.size() + " steps...");
		for (WdlStep step : steps) {
			executableNode = new ExecutableStep(step, context);
			log.info("[Job {}] Executor: execute step '{}'...", context.getJobId(), step.getName());
			executableNode.run();
			if (!executableNode.isSuccessful()) {
				return false;
			}
		}

		return true;
	}

	public void kill() {
		executableNode.kill();
	}

	public void updateProgress() {
		if (executableNode != null) {
			executableNode.updateProgress();
		}
	}

	public int getProgress() {
		if (executableNode != null) {
			return executableNode.getProgress();
		} else {
			return 0;
		}
	}

	public ExecutableStep getCurrentNode() {
		return executableNode;
	}

}
