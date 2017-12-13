package cloudgene.mapred.steps;

import genepi.hadoop.common.WorkflowContext;
import genepi.io.FileUtil;
import groovy.util.GroovyScriptEngine;
import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.wdl.WdlStep;

public class GroovyStep extends CloudgeneStep {

	static void runWithGroovyScriptEngine() throws Exception {
		// Declaring a class to conform to a java interface class would get rid
		// of
		// a lot of the reflection here

	}

	@Override
	public boolean run(WdlStep step, CloudgeneContext context) {
		context.setConfig(step);

		String script = step.get("script");
		String workingDirectory = context.getWorkingDirectory();

		String filename = FileUtil.path(workingDirectory, script);

		try {
			
			Class scriptClass = new GroovyScriptEngine(".", getClass().getClassLoader()).loadScriptByName(filename);
			Object scriptInstance = scriptClass.newInstance();
			Object result = scriptClass.getDeclaredMethod("run", new Class[] { WorkflowContext.class })
					.invoke(scriptInstance, new Object[] { context });
			if (result instanceof Boolean) {
				return (Boolean) result;
			} else {
				return true;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

}
