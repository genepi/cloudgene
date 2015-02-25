package cloudgene.mapred.resources.admin;

import net.sf.json.JSONObject;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

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
		object.put("maintenance", settings.isMaintenance());
		object.put("blocked", !WorkflowEngine.getInstance().isRunning());
		object.put("threads", settings.getThreadsQueue());
		object.put("max_jobs", settings.getMaxRunningJobs());
		object.put("max_jobs_user", settings.getMaxRunningJobsPerUser());

		return new StringRepresentation(object.toString(),
				MediaType.APPLICATION_JSON);

	}
}
