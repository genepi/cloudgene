package cloudgene.mapred.jobs;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import cloudgene.mapred.util.Settings;
import cloudgene.mapred.util.Technology;
import cloudgene.mapred.wdl.WdlApp;
import genepi.io.FileUtil;

public class Environment {

	public static Map<String, String> getApplicationVariables(WdlApp application, Settings settings) {

		String hdfsAppFolder = settings.getHdfsAppWorkspace();

		HashMap<String, String> environment = new HashMap<String, String>();
		String hdfsFolder = FileUtil.path(hdfsAppFolder, application.getId(), application.getVersion());
		String localFolder = application.getPath();
		environment.put("app_hdfs_folder", hdfsFolder);
		environment.put("app_local_folder", localFolder);
		// Deprecated
		environment.put("hdfs_app_folder", hdfsFolder);
		environment.put("local_app_folder", localFolder);
		// Technologies
		environment.put("docker_installed", settings.isEnable(Technology.DOCKER) ? "true" : "false");
		environment.put("hadoop_installed", settings.isEnable(Technology.HADOOP_CLUSTER) ? "true" : "false");
		environment.put("r_markdown_installed", settings.isEnable(Technology.R_MARKDOWN) ? "true" : "false");

		return environment;
	}

	public static Map<String, String> getJobVariables(CloudgeneContext context) {
		Map<String, String> environment = new HashMap<String, String>();
		environment.put("job_id", context.getJobId());
		environment.put("job_local_temp", context.getLocalTemp());
		environment.put("job_hdfs_temp", context.getHdfsTemp());
		environment.put("job_local_output", context.getLocalOutput());
		environment.put("job_hdfs_output", context.getHdfsOutput());
		environment.put("user_username", context.getUser().getUsername());
		environment.put("user_mail", context.getUser().getMail());
		// Deprecated
		environment.put("workdir", new File(context.getWorkingDirectory()).getAbsolutePath());
		environment.put("jobId", context.getJobId());

		return environment;
	}

	public static String env(String value, Map<String, String> variables) {

		for (String key : variables.keySet()) {
			value = value.replaceAll("\\$\\{" + key + "\\}", variables.get(key));
		}

		return value;
	}

}
