package cloudgene.mapred.steps;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.jobs.sdk.WorkflowContext;
import cloudgene.mapred.wdl.WdlStep;
import genepi.io.FileUtil;
import groovy.util.GroovyScriptEngine;

public class GroovyStep extends CloudgeneStep {

	private static final Logger log = LoggerFactory.getLogger(GroovyStep.class);

	
	@Override
	public boolean run(WdlStep step, CloudgeneContext context) {
		context.setConfig(step);

		String script = step.getString("script");
		String workingDirectory = context.getWorkingDirectory();

		String filename = FileUtil.path(workingDirectory, script);

		try {

			Class scriptClass = new GroovyScriptEngine(".", getClass().getClassLoader()).loadScriptByName(filename);
			Object scriptInstance = scriptClass.newInstance();

			Method method = scriptClass.getDeclaredMethod("run", new Class[] { WorkflowContext.class });
			Object result = method.invoke(scriptInstance, new Object[] { context });
			if (result instanceof Boolean) {
				return (Boolean) result;
			} else {
				return true;
			}

		} catch (Exception e) {
			if (e.getCause() != null) {
				log.error("[Job {}] Step '{}': Error in script '{}'", context.getJobId(), step.getName(), script, e.getCause());
				context.error("Error in script " + script + ":\n" + getStackTraceAsString(e.getCause()));
			} else {
				log.error("[Job {}] Step '{}': Error in script '{}'", context.getJobId(), step.getName(), script, e);
				context.error("Error in script " + script + ":\n" + getStackTraceAsString(e));
			}
			return false;
		}

	}

	public static String getStackTraceAsString(Throwable throwable) {
		StringWriter stringWriter = new StringWriter();
		throwable.printStackTrace(new PrintWriter(stringWriter));
		return stringWriter.toString();
	}

}
