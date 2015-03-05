package cloudgene.mapred.resources.admin;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import net.sf.json.JSONObject;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.Main;
import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.util.Settings;

public class GetClusterDetails extends ServerResource {

	@Get
	public Representation get() {

		Settings settings = Settings.getInstance();

		UserSessions sessions = UserSessions.getInstance();
		User user = sessions.getUserByRequest(getRequest());

		if (user == null) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");
		}

		if (!user.isAdmin()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires administration rights.");
		}

		JSONObject object = new JSONObject();
		object.put("version", Main.VERSION);

		URLClassLoader cl = (URLClassLoader) Main.class.getClassLoader();
		try {
			URL url = cl.findResource("META-INF/MANIFEST.MF");
			Manifest manifest = new Manifest(url.openStream());
			Attributes attr = manifest.getMainAttributes();
			String buildVesion = attr.getValue("Version");
			String buildTime = attr.getValue("Build-Time");
			String builtBy = attr.getValue("Built-By");
			object.put("built_by", builtBy);
			object.put("built_time", buildTime);

		} catch (IOException E) {
			// handle
		}

		object.put("maintenance", settings.isMaintenance());
		object.put("blocked", !WorkflowEngine.getInstance().isRunning());
		object.put("threads", settings.getThreadsQueue());
		object.put("max_jobs", settings.getMaxRunningJobs());
		object.put("max_jobs_user", settings.getMaxRunningJobsPerUser());

		return new StringRepresentation(object.toString(),
				MediaType.APPLICATION_JSON);

	}
}
