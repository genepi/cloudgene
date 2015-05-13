package cloudgene.mapred.resources.admin;

import java.util.List;
import java.util.Vector;

import net.sf.json.JSONArray;
import net.sf.json.JsonConfig;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.util.BaseResource;

public class GetAllJobs extends BaseResource {

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

		if (!user.isAdmin()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires administration rights.");
		}

		String state = "";
		if (getQuery().getFirst("state") != null) {
			state = getQuery().getFirst("state").getValue();
		}

		WorkflowEngine engine = getWorkflowEngine();
		JobDao dao = new JobDao(getDatabase());
		List<AbstractJob> jobs = new Vector<AbstractJob>();

		switch (state) {

		case "running-ltq":

			jobs = engine.getAllJobsInLongTimeQueue();
			break;

		case "running-stq":

			jobs = engine.getAllJobsInShortTimeQueue();
			break;

		case "current":

			jobs = dao.findAllNotRetiredJobs();
			List<AbstractJob> toRemove = new Vector<AbstractJob>();
			for (AbstractJob job : jobs) {
				if (engine.isInQueue(job)) {
					toRemove.add(job);
				}
			}
			jobs.removeAll(toRemove);
			break;

		case "retired":

			jobs = dao.findAllByState(AbstractJob.STATE_RETIRED);
			break;

		}

		JsonConfig config = new JsonConfig();
		config.setExcludes(new String[] { "outputParams", "inputParams",
				"output", "endTime", "startTime", "error", "s3Url", "task",
				"config", "mapReduceJob", "job", "step", "context",
				"hdfsWorkspace", "localWorkspace", "logOutFiles", "logs",
				"removeHdfsWorkspace", "settings", "setupComplete",
				"stdOutFile", "steps", "workingDirectory", "activationCode",
				"active", "admin", "awsKey", "awsSecretKey", "exportInputToS3",
				"exportToS3", "password", "s3Bucket", "saveCredentials","map","reduce" });
		JSONArray jsonArray = JSONArray.fromObject(jobs, config);

		return new StringRepresentation(jsonArray.toString());

	}
}
