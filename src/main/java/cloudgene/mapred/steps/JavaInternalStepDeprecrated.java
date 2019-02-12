package cloudgene.mapred.steps;

import java.util.Map;
import java.util.Set;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.wdl.WdlStep;
import genepi.hadoop.common.WorkflowContext;
import genepi.hadoop.common.WorkflowStep;

public class JavaInternalStepDeprecrated extends CloudgeneStep {

	private WorkflowStep workflowStep;

	public JavaInternalStepDeprecrated(WorkflowStep step) {
		this.workflowStep = step;
	}

	@Override
	public void setup(CloudgeneContext context) {
		workflowStep.setup(adapter(context));
	}

	@Override
	public void kill() {
		workflowStep.kill();
	}

	@Override
	public void updateProgress() {
		workflowStep.updateProgress();
	}

	@Override
	public boolean run(WdlStep step, CloudgeneContext context) {
		context.setConfig(step);
		return workflowStep.run(adapter(context));
	}

	public static genepi.hadoop.common.WorkflowContext adapter(CloudgeneContext context) {
		return new WorkflowContext() {
			
			@Override
			public void updateTask(String name, int type) {
				context.updateTask(name, type);				
			}
			
			@Override
			public void submitCounter(String name) {
				context.submitCounter(name);				
			}
			
			@Override
			public void setOutput(String input, String value) {
				context.setOutput(input, value);				
			}
			
			@Override
			public void setInput(String input, String value) {
				context.setInput(input, value);				
			}
			
			@Override
			public void setConfig(Map<String, String> config) {
				context.setConfig(config);				
			}
			
			@Override
			public boolean sendNotification(String body) throws Exception {
				return context.sendNotification(body);
			}
			
			@Override
			public boolean sendMail(String to, String subject, String body) throws Exception {
				return context.sendMail(to, subject, body);
			}
			
			@Override
			public boolean sendMail(String subject, String body) throws Exception {
				return context.sendMail(subject, body);
			}
			
			@Override
			public void println(String line) {
				context.println(line);				
			}
			
			@Override
			public void message(String message, int type) {
				context.message(message, type);				
			}
			
			@Override
			public void log(String line) {
				context.log(line);				
			}
			
			@Override
			public void incCounter(String name, int value) {
				context.incCounter(name, value);				
			}
			
			@Override
			public String getWorkingDirectory() {				
				return context.getWorkingDirectory();
			}
			
			@Override
			public String getOutput(String param) {
				return context.getOutput(param);
			}
			
			@Override
			public String getLocalTemp() {
				return context.getLocalTemp();
			}
			
			@Override
			public String getJobName() {				
				return context.getJobName();
			}
			
			@Override
			public String getJobId() {
				return context.getJobId();
			}
			
			@Override
			public Set<String> getInputs() {
				return context.getInputs();
			}
			
			@Override
			public String getInput(String param) {
				return context.getInput(param);
			}
			
			@Override
			public String getHdfsTemp() {
				return context.getHdfsTemp();
			}
			
			@Override
			public Object getData(String key) {
				return context.getData(key);
			}
			
			@Override
			public Map<String, Integer> getCounters() {
				return context.getCounters();
			}
			
			@Override
			public String getConfig(String param) {
				return context.getConfig(param);
			}
			
			@Override
			public String get(String param) {
				return context.get(param);
			}
			
			@Override
			public void endTask(String message, int type) {
				context.endTask(message, type);				
			}
			
			@Override
			public String createLinkToFile(String id) {
				return context.createLinkToFile(id);
			}
			
			@Override
			public void beginTask(String name) {
				context.beginTask(name);				
			}
		};
	}
	
}
