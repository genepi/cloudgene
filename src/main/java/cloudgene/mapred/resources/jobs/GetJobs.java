package cloudgene.mapred.resources.jobs;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JsonConfig;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.Settings;

public class GetJobs extends ServerResource {

	/**
	 * Resource to get job status information
	 */

	@Get
	public Representation getJobs() {

		Settings settings = Settings.getInstance();

		UserSessions sessions = UserSessions.getInstance();
		User user = sessions.getUserByRequest(getRequest());

		if (user == null) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");
		}

		if (settings.isMaintenance() && !user.isAdmin()) {

			setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
			return new StringRepresentation(
					"This functionality is currently under maintenance.");

		}

		// jobs in queue
		//WorkflowEngine engine = WorkflowEngine.getInstance();
		//List<AbstractJob> jobs = engine.getJobsByUser(user);

		// complete jobs
		JobDao dao = new JobDao();
		List<AbstractJob> jobs = dao.findAllByUser(user);
		//jobs.addAll(oldJobs);

		// exclude unused parameters
		JsonConfig config = new JsonConfig();
		config.setExcludes(new String[] { "user", "outputParams",
				"inputParams", "output", "endTime", "startTime", "error",
				"s3Url", "task", "config", "mapReduceJob", "job", "step",
				"context" });

		JSONArray jsonArray = JSONArray.fromObject(jobs, config);

		return new StringRepresentation(jsonArray.toString());

	}
}
