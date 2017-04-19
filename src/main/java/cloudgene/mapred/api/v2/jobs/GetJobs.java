package cloudgene.mapred.api.v2.jobs;

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
	 * Resource to get a list of all jobs for authenticated user.
	 */

	@Get
	public Representation getJobs() {

		User user = getAuthUser();

		if (user == null) {
			return error401("The request requires user authentication.");
		}

		if (getSettings().isMaintenance() && !user.isAdmin()) {

			return error(Status.SERVER_ERROR_SERVICE_UNAVAILABLE,
					"This functionality is currently under maintenance.");

		}

		// find all jobs by user
		JobDao dao = new JobDao(getDatabase());
		List<AbstractJob> jobs = dao.findAllByUser(user);

		// exclude unused parameters
		JsonConfig config = new JsonConfig();
		config.setExcludes(new String[] { "user", "outputParams",
				"inputParams", "output", "endTime", "startTime", "error",
				"s3Url", "task", "config", "mapReduceJob", "job", "step",
				"context", "hdfsWorkspace", "localWorkspace", "logOutFiles",
				"logs", "removeHdfsWorkspace", "settings", "setupComplete",
				"stdOutFile", "steps", "workingDirectory", "application",
				"map", "reduce", "logOutFile", "deletedOn","applicationId","running" });

		JSONArray jsonArray = JSONArray.fromObject(jobs, config);

		return new StringRepresentation(jsonArray.toString());

	}
}
