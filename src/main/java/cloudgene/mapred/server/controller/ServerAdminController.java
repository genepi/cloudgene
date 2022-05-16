package cloudgene.mapred.server.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import cloudgene.mapred.core.User;
import cloudgene.mapred.plugins.IPlugin;
import cloudgene.mapred.plugins.PluginManager;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.auth.AuthenticationService;
import cloudgene.mapred.server.responses.ServerResponse;
import cloudgene.mapred.server.services.ServerService;
import cloudgene.mapred.util.Settings;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.oauth2.configuration.OauthClientConfigurationProperties;
import jakarta.inject.Inject;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller("/api/v2/admin/server")
@Secured(User.ROLE_ADMIN)

public class ServerAdminController {

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Inject
	protected List<OauthClientConfigurationProperties> clients;

	@Inject
	protected ServerService serverService;

	@Get("/queue/block")
	@Produces(MediaType.TEXT_PLAIN)
	public String blockQueue() {
		application.getWorkflowEngine().block();
		return "Queue blocked.";
	}

	@Get("/queue/open")
	@Produces(MediaType.TEXT_PLAIN)
	public String openQueue() {
		application.getWorkflowEngine().resume();
		return "Queue opened.";
	}

	@Get("/maintenance/enter")
	@Produces(MediaType.TEXT_PLAIN)
	public String enterMaintenance() {
		application.getSettings().setMaintenance(true);
		application.getSettings().save();
		return "Enter Maintenance mode.";
	}

	@Get("/maintenance/exit")
	@Produces(MediaType.TEXT_PLAIN)
	public String exitMaintenance() {
		application.getSettings().setMaintenance(false);
		application.getSettings().save();
		return "Exit Maintenance mode.";
	}

	@Get("/cluster")
	public String getDetails() {

		JSONObject object = new JSONObject();

		// general settings
		object.put("maintenance", application.getSettings().isMaintenance());
		object.put("blocked", !application.getWorkflowEngine().isRunning());
		object.put("version", Application.VERSION);
		object.put("maintenance", application.getSettings().isMaintenance());
		object.put("blocked", !application.getWorkflowEngine().isRunning());
		object.put("threads_setup", application.getSettings().getThreadsSetupQueue());
		object.put("threads", application.getSettings().getThreadsQueue());
		object.put("max_jobs_user", application.getSettings().getMaxRunningJobsPerUser());
		try {
			URL url = ServerAdminController.class.getClassLoader().getResource("META-INF/MANIFEST.MF");
			Manifest manifest = new Manifest(url.openStream());
			Attributes attr = manifest.getMainAttributes();
			String buildVesion = attr.getValue("Version");
			String buildTime = attr.getValue("Build-Time");
			String builtBy = attr.getValue("Built-By");
			object.put("built_by", builtBy);
			object.put("built_time", buildTime);

		} catch (IOException E) {
			object.put("built_by", "Development");
			object.put("built_time", new Date().toGMTString());
		}

		// workspace and hdd
		File workspace = new File(application.getSettings().getLocalWorkspace());
		object.put("workspace_path", workspace.getAbsolutePath());
		object.put("free_disc_space", workspace.getUsableSpace() / 1024 / 1024 / 1024);
		object.put("total_disc_space", workspace.getTotalSpace() / 1024 / 1024 / 1024);
		object.put("used_disc_space",
				(workspace.getTotalSpace() / 1024 / 1024 / 1024) - (workspace.getUsableSpace() / 1024 / 1024 / 1024));
		// plugins
		PluginManager manager = PluginManager.getInstance();
		JSONArray pluginsArray = new JSONArray();
		for (IPlugin plugin : manager.getPlugins()) {
			JSONObject pluginObject = new JSONObject();
			pluginObject.put("name", plugin.getName());
			if (plugin.isInstalled()) {
				pluginObject.put("enabled", true);
				pluginObject.put("details", plugin.getDetails());
			} else {
				pluginObject.put("enabled", false);
				pluginObject.put("error", plugin.getStatus());
			}
			pluginsArray.add(pluginObject);
		}
		object.put("plugins", pluginsArray);

		// database
		object.put("db_max_active", application.getDatabase().getDataSource().getMaxActive());
		object.put("db_active", application.getDatabase().getDataSource().getNumActive());
		object.put("db_max_idle", application.getDatabase().getDataSource().getMaxIdle());
		object.put("db_idle", application.getDatabase().getDataSource().getNumIdle());
		object.put("db_max_open_prep_statements",
				application.getDatabase().getDataSource().getMaxOpenPreparedStatements());

		return object.toString();

	}

	@Get("/logs/{logfile}")
	public String getLogs(String logfile) {

		String content = serverService.tail(new File(logfile), 1000);
		return content;

	}

	@Get("/settings")
	public ServerResponse getSettings() {
		
		return ServerResponse.build( application.getSettings());

		/*
		 * JSONObject object = new JSONObject(); object.put("name",
		 * application.getSettings().getName()); object.put("background-color",
		 * application.getSettings().getColors().get("background"));
		 * object.put("foreground-color",
		 * application.getSettings().getColors().get("foreground"));
		 * object.put("google-analytics",
		 * application.getSettings().getGoogleAnalytics());
		 * 
		 * Map<String, String> mail = application.getSettings().getMail(); if
		 * (application.getSettings().getMail() != null) { object.put("mail", true);
		 * object.put("mail-smtp", mail.get("smtp")); object.put("mail-port",
		 * mail.get("port")); object.put("mail-user", mail.get("user"));
		 * object.put("mail-password", mail.get("password")); object.put("mail-name",
		 * mail.get("name")); } else { object.put("mail", false);
		 * object.put("mail-smtp", ""); object.put("mail-port", "");
		 * object.put("mail-user", ""); object.put("mail-password", "");
		 * object.put("mail-name", ""); }
		 * 
		 * return object.toString();
		 */

	}

	@Post("/settings/update")
	public ServerResponse updateSettings(String name, String backgroundColor, String foregroundColor, String googleAnalytics,
			boolean mail, String mailSmtp, String mailPort, String mailName) {
		
		serverService.updateSettings(name, backgroundColor, foregroundColor, googleAnalytics, String.valueOf(mail), mailSmtp,
				mailPort, "", "", mailName);
		
		return ServerResponse.build( application.getSettings());
		
		/*
		 * JSONObject object = new JSONObject();
		 * 
		 * object.put("name", application.getSettings().getName());
		 * object.put("background-color",
		 * application.getSettings().getColors().get("background"));
		 * object.put("foreground-color",
		 * application.getSettings().getColors().get("foreground"));
		 * object.put("google-analytics",
		 * application.getSettings().getGoogleAnalytics());
		 * 
		 * Map<String, String> mailConfig = application.getSettings().getMail(); if
		 * (application.getSettings().getMail() != null) { object.put("mail", true);
		 * object.put("mail-smtp", mailConfig.get("smtp")); object.put("mail-port",
		 * mailConfig.get("port")); object.put("mail-user", mailConfig.get("user"));
		 * object.put("mail-password", mailConfig.get("password"));
		 * object.put("mail-name", mailConfig.get("name")); } else { object.put("mail",
		 * false); object.put("mail-smtp", ""); object.put("mail-port", "");
		 * object.put("mail-user", ""); object.put("mail-password", "");
		 * object.put("mail-name", ""); }
		 * 
		 * return object.toString();
		 */

	}

	@Get("/cloudgene-apps")
	public String list() throws ResourceException, IOException {

		ClientResource clientResource = new ClientResource("http://apps.cloudgene.io/api/apps.json");
		return clientResource.get().getText();

	}

}
