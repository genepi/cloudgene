package cloudgene.mapred.resources.apps;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.TaskJob;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.tasks.AbstractTask;
import cloudgene.mapred.tasks.ImporterApp;

public class InstallApp extends ServerResource {

	@Post
	public Representation post(Representation entity) {

		StringRepresentation representation = null;

		try {
			JsonRepresentation represent = new JsonRepresentation(entity);

			UserSessions sessions = UserSessions.getInstance();
			User user = sessions.getUserByRequest(getRequest());
			JSONObject obj = represent.getJsonObject();

			if (user != null) {

				String url = obj.get("package-url").toString();

				AbstractTask task = new ImporterApp(url);
				AbstractJob job = new TaskJob(task);
				job.setName(task.getName());
				job.setUser(user);

				WorkflowEngine queue = WorkflowEngine.getInstance();
				queue.submit(job);

				// Response

				representation = new StringRepresentation(
						"Application import started!");
				getResponse().setStatus(Status.SUCCESS_OK);
				getResponse().setEntity(representation);
				return representation;

			} else {
				representation = new StringRepresentation("No user");
				getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				getResponse().setEntity(representation);
				return representation;
			}
		} catch (JSONException e) {
			representation = new StringRepresentation(e.getMessage());
			getResponse().setEntity(representation);
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			e.printStackTrace();
			return representation;
		} catch (IOException e) {
			representation = new StringRepresentation(e.getMessage());
			getResponse().setEntity(representation);
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			e.printStackTrace();
			return representation;
		}

	}

	/**
	 * Mandatory. Specifies that this resource supports POST requests.
	 */
	public boolean allowPost() {
		return true;
	}

}
