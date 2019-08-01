package cloudgene.mapred.jobs;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import cloudgene.mapred.plugins.IPlugin;
import cloudgene.mapred.plugins.PluginManager;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlApp;
import genepi.io.FileUtil;

public class Environment {

	public static Map<String, String> getApplicationVariables(WdlApp application, Settings settings) {

		String hdfsAppFolder = settings.getHdfsAppWorkspace();

		HashMap<String, String> environment = new HashMap<String, String>();
		String hdfsFolder = FileUtil.path(hdfsAppFolder, application.getId().split(":")[0], application.getVersion());
		String localFolder = application.getPath();
		environment.put("app_id", application.getId());
		environment.put("app_name", application.getName());
		environment.put("app_version", application.getVersion());
		environment.put("app_hdfs_folder", hdfsFolder);
		environment.put("app_local_folder", localFolder);
		// Deprecated
		environment.put("hdfs_app_folder", hdfsFolder);
		environment.put("local_app_folder", localFolder);
		// Technologies
		PluginManager manager = PluginManager.getInstance();
		for (IPlugin plugin : manager.getPlugins()) {
			environment.put(plugin.getId() + "_installed", manager.isEnabled(plugin) ? "true" : "false");
		}

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
