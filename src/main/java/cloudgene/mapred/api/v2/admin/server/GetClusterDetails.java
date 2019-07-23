package cloudgene.mapred.api.v2.admin.server;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.Main;
import cloudgene.mapred.core.User;
import cloudgene.mapred.plugins.IPlugin;
import cloudgene.mapred.plugins.PluginManager;
import cloudgene.mapred.util.BaseResource;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class GetClusterDetails extends BaseResource {

	@Get
	public Representation get() {

		User user = getAuthUser();

		if (user == null) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires user authentication.");
		}

		if (!user.isAdmin()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires administration rights.");
		}

		JSONObject object = new JSONObject();

		// general settigns
		object.put("maintenance", getSettings().isMaintenance());
		object.put("blocked", !getWorkflowEngine().isRunning());
		object.put("version", Main.VERSION);
		object.put("maintenance", getSettings().isMaintenance());
		object.put("blocked", !getWorkflowEngine().isRunning());
		object.put("threads_setup", getSettings().getThreadsSetupQueue());
		object.put("threads", getSettings().getThreadsQueue());
		object.put("max_jobs_user", getSettings().getMaxRunningJobsPerUser());
		try {
			URL url = GetClusterDetails.class.getClassLoader().getResource("META-INF/MANIFEST.MF");
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
		File workspace = new File(getSettings().getLocalWorkspace());
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
		object.put("db_max_active", getDatabase().getDataSource().getMaxActive());
		object.put("db_active", getDatabase().getDataSource().getNumActive());
		object.put("db_max_idle", getDatabase().getDataSource().getMaxIdle());
		object.put("db_idle", getDatabase().getDataSource().getNumIdle());
		object.put("db_max_open_prep_statements", getDatabase().getDataSource().getMaxOpenPreparedStatements());

		JSONObject hostnames = new JSONObject();
		if (getRequest().getHostRef() != null) {
			hostnames.put("host_ref", getRequest().getHostRef().getHostIdentifier());
		}
		if (getRequest().getOriginalRef() != null) {
			hostnames.put("original_ref", getRequest().getOriginalRef().getHostIdentifier());
		}
		if (getRequest().getResourceRef() != null) {
			hostnames.put("resource_ref", getRequest().getResourceRef().getHostIdentifier());
		}
		if (getRequest().getRootRef() != null) {
			hostnames.put("root_ref", getRequest().getRootRef().getHostIdentifier());
		}
		if (getRequest().getReferrerRef() != null) {
			hostnames.put("referrer_ref", getRequest().getReferrerRef().getHostIdentifier());
		}

		object.put("hostnames", hostnames);

		return new StringRepresentation(object.toString(), MediaType.APPLICATION_JSON);

	}
}
