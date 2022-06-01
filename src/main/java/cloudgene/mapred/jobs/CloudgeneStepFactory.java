package cloudgene.mapred.jobs;

import cloudgene.mapred.steps.*;
import cloudgene.mapred.wdl.WdlStep;

import java.util.HashMap;
import java.util.Map;

public class CloudgeneStepFactory {

	private static CloudgeneStepFactory instance = null;
	
	private Map<String, String> registeredClasses;

	public static CloudgeneStepFactory getInstance() {
		if (instance == null) {
			instance = new CloudgeneStepFactory();
		}
		return instance;
	}
	
	private CloudgeneStepFactory() {
		registeredClasses = new HashMap<String, String>();
	}
	
	public void register(String type, Class clazz) {
		registeredClasses.put(type, clazz.getName());
	}
	
	public String getClassname(WdlStep step) {

		String type = step.get("type");

		if (type != null) {
			
			String clazz = registeredClasses.get(type);
			if (clazz != null) {
				return clazz;
			}
			
			switch (type.toLowerCase()) {
			case "java":
				return JavaExternalStep.class.getName();
			case "rmd_docker":
				return RMarkdownDockerStep.class.getName();
			case "groovy":
				return GroovyStep.class.getName();
			case "html_widget":
				return HtmlWidgetStep.class.getName();
			}
		}

		if (step.get("pig") != null) {

			// pig script
			return HadoopPigStep.class.getName();

		}
		if (step.get("spark") != null) {

			// spark
			return HadoopSparkStep.class.getName();

		} else if (step.get("rmd") != null) {

			// rscript
			return RMarkdownStep.class.getName();

		} else if (step.get("rmd2") != null) {

			// rscript
			return RMarkdownStep.class.getName();

		} else if (step.getClassname() != null) {

			// custom class
			return step.getClassname();

		} else if (step.get("exec") != null || step.get("cmd") != null) {

			// command
			return BashCommandStep.class.getName();

		} else {
			String runtime = step.get("runtime");
			if (runtime == null || runtime.isEmpty() || runtime.toLowerCase().equals("hadoop")) {
				// mapreduce
				return HadoopMapReduceStep.class.getName();
			} else if (runtime != null && runtime.toLowerCase().equals("java")) {
				// normal java when no Hadoop suppport
				return JavaExternalStep.class.getName();
			}
		}

		return null;
	}

}
