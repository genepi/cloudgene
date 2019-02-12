package cloudgene.mapred.steps;

import java.lang.reflect.Method;

import com.google.common.base.Throwables;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.wdl.WdlStep;
import cloudgene.sdk.internal.WorkflowContext;
import genepi.io.FileUtil;
import groovy.util.GroovyScriptEngine;

public class GroovyStep extends CloudgeneStep {

	@Override
	public boolean run(WdlStep step, CloudgeneContext context) {
		context.setConfig(step);

		String script = step.get("script");
		String workingDirectory = context.getWorkingDirectory();

		String filename = FileUtil.path(workingDirectory, script);

		try {

			Class scriptClass = new GroovyScriptEngine(".", getClass().getClassLoader()).loadScriptByName(filename);
			Object scriptInstance = scriptClass.newInstance();

			try {
				Method method = scriptClass.getDeclaredMethod("run", new Class[] { WorkflowContext.class });
				Object result = method.invoke(scriptInstance, new Object[] { context });
				if (result instanceof Boolean) {
					return (Boolean) result;
				} else {
					return true;
				}
			} catch (NoSuchMethodException e) {

				// old dependency to genepi.hadoop
				Method method = scriptClass.getDeclaredMethod("run",
						new Class[] { genepi.hadoop.common.WorkflowContext.class });
				Object result = method.invoke(scriptInstance,
						new Object[] { JavaInternalStepDeprecrated.adapter(context) });
				if (result instanceof Boolean) {
					return (Boolean) result;
				} else {
					return true;
				}

			}

		} catch (Exception e) {
			if (e.getCause() != null) {
				context.error("Error in script " + script + ":\n" + Throwables.getStackTraceAsString(e.getCause()));
			} else {
				context.error("Error in script " + script + ":\n" + Throwables.getStackTraceAsString(e));
			}
			return false;
		}

	}

}
