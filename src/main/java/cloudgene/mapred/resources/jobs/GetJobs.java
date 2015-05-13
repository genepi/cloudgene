package cloudgene.mapred.resources.jobs;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JsonConfig;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.BaseResource;

public class GetJobs extends BaseResource {

	/**
	 * Resource to get job status information
	 */

	@Get
	public Representation getJobs() {

		User user = getUser(getRequest());

		if (user == null) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");
		}

		if (getSettings().isMaintenance() && !user.isAdmin()) {

			setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
			return new StringRepresentation(
					"This functionality is currently under maintenance.");

		}

		// jobs in queue
		// WorkflowEngine engine = WorkflowEngine.getInstance();
		// List<AbstractJob> jobs = engine.getJobsByUser(user);

		// complete jobs
		JobDao dao = new JobDao(getDatabase());
		List<AbstractJob> jobs = dao.findAllByUser(user);
		// jobs.addAll(oldJobs);

		// exclude unused parameters
		JsonConfig config = new JsonConfig();
		config.setExcludes(new String[] { "user", "outputParams",
				"inputParams", "output", "endTime", "startTime", "error",
				"s3Url", "task", "config", "mapReduceJob", "job", "step",
				"context", "hdfsWorkspace", "localWorkspace", "logOutFiles",
				"logs", "removeHdfsWorkspace", "settings", "setupComplete",
				"stdOutFile", "steps", "workingDirectory", "application",
				"map", "reduce", "logOutFile", "deletedOn" });

		JSONArray jsonArray = JSONArray.fromObject(jobs, config);

		return new StringRepresentation(jsonArray.toString());

	}
}
