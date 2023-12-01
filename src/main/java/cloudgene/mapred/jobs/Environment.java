package cloudgene.mapred.jobs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import cloudgene.mapred.plugins.IPlugin;
import cloudgene.mapred.plugins.PluginManager;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlApp;
import com.fasterxml.jackson.annotation.JsonClassDescription;

public class Environment {

	public static String PREFIX = "CLOUDGENE_";

	private Map<String, String> env = new HashMap<String, String>();

	public Environment(Settings settings) {
		add("SERVICE_NAME", settings.getName());
		add("SERVICE_URL", settings.getServerUrl() + settings.getBaseUrl());
		add("CONTACT_EMAIL", settings.getAdminMail());
		add("CONTACT_NAME", settings.getAdminName());
		if (settings.getMail() != null) {
			add("SMTP_HOST", settings.getMail().get("smtp"));
			add("SMTP_PORT", settings.getMail().get("port"));
			add("SMTP_USER", settings.getMail().get("user"));
			add("SMTP_PASSWORD", settings.getMail().get("password"));
			add("SMTP_NAME", settings.getMail().get("name"));
			add("SMTP_SENDER", settings.getMail().get("name"));
		}
		add("WORKSPACE_TYPE", settings.getExternalWorkspaceType());
		add("WORKSPACE_HOME", settings.getExternalWorkspaceLocation());
	}

	public Environment addContext(CloudgeneContext context) {
		add("JOB_ID", context.getJobId());
		add("JOB_NAME", context.getJobName());
		add("USER_NAME", context.getUser().getUsername());
		add("USER_EMAIL", context.getUser().getMail());
		add("USER_FULL_NAME", context.getUser().getFullName());
		return this;
	}
	
	public Environment addApplication(WdlApp application) {
		String localFolder = application.getPath();
		add("APP_LOCATION", localFolder);
		//old variable names without prefix for compatibility
		env.put("app_id", application.getId());
		env.put("app_name", application.getName());
		env.put("app_version", application.getVersion());
		env.put("app_local_folder", localFolder);
		env.put("local_app_folder", localFolder);
		env.put("app_hdfs_folder", localFolder);
		return this;
	}

	public Environment add(String name, String value) {
		env.put(PREFIX + name, value != null ? value : "");
		return this;
	}

	public Map<String, String> toMap() {
		return env;
	}

	public List<Variable> toList() {
		List<Variable> variables = new Vector<Variable>();
		for (Entry<String, String> entry : env.entrySet()) {
			if (entry.getKey().endsWith("_PASSWORD")) {
				variables.add(new Variable(entry.getKey(), "************"));
			} else {
				variables.add(new Variable(entry.getKey(), entry.getValue()));
			}
		}
		return variables;
	}
	
	
	public static String resolve(String value, Map<String, String> variables) {
		for (String key : variables.keySet()) {
			value = value.replaceAll("\\$\\{" + key + "\\}", variables.get(key));
		}
		return value;
	}

	public String resolve(String value) {
		for (String key : env.keySet()) {
			value = value.replaceAll("\\$\\{" + key + "\\}", env.get(key));
		}
		return value;
	}

	@JsonClassDescription
	public static class Variable {

		private String name;

		private String value;

		public Variable(String name, String value) {
			this.name = name;
			this.value = value;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

	}

}
