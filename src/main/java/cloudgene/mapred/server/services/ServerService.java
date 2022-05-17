package cloudgene.mapred.server.services;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cloudgene.mapred.plugins.IPlugin;
import cloudgene.mapred.plugins.PluginManager;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.controller.ServerAdminController;
import cloudgene.mapred.util.Settings;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ServerService {
	
	public static final String IMAGE_DATA = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"96\" height=\"20\">"
			+ "	<linearGradient id=\"b\" x2=\"0\" y2=\"100%\"><stop offset=\"0\" stop-color=\"#bbb\" stop-opacity=\".1\"/><stop offset=\"1\" stop-opacity=\".1\"/></linearGradient>"
			+ "	<mask id=\"a\"><rect width=\"96\" height=\"20\" rx=\"3\" fill=\"#fff\"/></mask>"
			+ "	<g mask=\"url(#a)\"><path fill=\"#555\" d=\"M0 0h55v20H0z\"/><path fill=\"#97CA00\" d=\"M55 0h41v20H55z\"/><path fill=\"url(#b)\" d=\"M0 0h96v20H0z\"/></g>"
			+ "	<g fill=\"#fff\" text-anchor=\"middle\" font-family=\"DejaVu Sans,Verdana,Geneva,sans-serif\" font-size=\"11\">"
			+ "		<text x=\"27.5\" y=\"15\" fill=\"#010101\" fill-opacity=\".3\">version</text>"
			+ "		<text x=\"27.5\" y=\"14\">version</text>"
			+ "		<text x=\"74.5\" y=\"15\" fill=\"#010101\" fill-opacity=\".3\">" + Application.VERSION + "</text>"
			+ "		<text x=\"74.5\" y=\"14\">" + Application.VERSION + "</text>" + "	</g>" + "</svg>";
	
	@Inject
	protected Application application;
	
	
	public void updateSettings (String name, String background_color, String foreground_color, String google_analytics, String mail, String mail_smtp, String mail_port, String mail_user,  String mail_password, String mail_name) {
		
		Settings settings = application.getSettings();
		settings.setName(name);
		settings.getColors().put("background", background_color);
		settings.getColors().put("foreground", foreground_color);
		settings.setGoogleAnalytics(google_analytics);

		if (mail != null && mail.equals("true")) {
			Map<String, String> mailConfig = new HashMap<String, String>();
			mailConfig.put("smtp", mail_smtp);
			mailConfig.put("port", mail_port);
			mailConfig.put("user", mail_user);
			mailConfig.put("password", mail_password);
			mailConfig.put("name", mail_name);
			application.getSettings().setMail(mailConfig);
		} else {
			application.getSettings().setMail(null);
		}

		application.getSettings().save();
		
	}
	
	public String getClusterDetails() {
		
		ObjectMapper mapper = new ObjectMapper();
		
	    ObjectNode object = mapper.createObjectNode();

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
		
		ArrayNode plugins = object.putArray("plugins");
		
		for (IPlugin plugin : manager.getPlugins()) {
			ObjectNode pluginObject = mapper.createObjectNode();
			pluginObject.put("name", plugin.getName());

			if (plugin.isInstalled()) {
				pluginObject.put("enabled", true);
				pluginObject.put("details", plugin.getDetails());
			} else {
				pluginObject.put("enabled", false);
				pluginObject.put("error", plugin.getStatus());
			}
			plugins.add(pluginObject);
		}
	
		// database
		object.put("db_max_active", application.getDatabase().getDataSource().getMaxActive());
		object.put("db_active", application.getDatabase().getDataSource().getNumActive());
		object.put("db_max_idle", application.getDatabase().getDataSource().getMaxIdle());
		object.put("db_idle", application.getDatabase().getDataSource().getNumIdle());
		object.put("db_max_open_prep_statements",
				application.getDatabase().getDataSource().getMaxOpenPreparedStatements());

		return object.toString();
	}
	
	
	
	public String tail(File file, int lines) {
		java.io.RandomAccessFile fileHandler = null;
		try {
			fileHandler = new java.io.RandomAccessFile(file, "r");
			long fileLength = fileHandler.length() - 1;
			StringBuilder sb = new StringBuilder();
			int line = 0;

			for (long filePointer = fileLength; filePointer != -1; filePointer--) {
				fileHandler.seek(filePointer);
				int readByte = fileHandler.readByte();

				if (readByte == 0xA) {
					line = line + 1;
					if (line == lines) {
						if (filePointer == fileLength) {
							continue;
						}
						break;
					}
				} else if (readByte == 0xD) {
					line = line + 1;
					if (line == lines) {
						if (filePointer == fileLength - 1) {
							continue;
						}
						break;
					}
				}
				sb.append((char) readByte);
			}

			String lastLine = sb.reverse().toString();
			return lastLine;
		} catch (java.io.FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (java.io.IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (fileHandler != null)
				try {
					fileHandler.close();
				} catch (IOException e) {
				}
		}
	}
	

}
