package cloudgene.mapred.jobs;

import cloudgene.mapred.steps.BashCommandStep;
import cloudgene.mapred.steps.DockerStep;
import cloudgene.mapred.steps.GroovyStep;
import cloudgene.mapred.steps.HadoopMapReduceStep;
import cloudgene.mapred.steps.HadoopPigStep;
import cloudgene.mapred.steps.HadoopSparkStep;
import cloudgene.mapred.steps.HtmlWidgetStep;
import cloudgene.mapred.steps.JavaExternalStep;
import cloudgene.mapred.steps.RMarkdown2DockerStep;
import cloudgene.mapred.steps.RMarkdown2Step;
import cloudgene.mapred.steps.RMarkdownStep;
import cloudgene.mapred.wdl.WdlStep;

public class CloudgeneStepFactory {

	public static String getClassname(WdlStep step) {

		String type = step.get("type");

		if (type != null) {
			switch (type.toLowerCase()) {
			case "java":
				return JavaExternalStep.class.getName();
			case "docker":
				return DockerStep.class.getName();
			case "rmd_docker":
				return RMarkdown2DockerStep.class.getName();
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
			return RMarkdown2Step.class.getName();

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
