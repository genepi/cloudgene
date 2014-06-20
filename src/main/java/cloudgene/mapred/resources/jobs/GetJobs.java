package cloudgene.mapred.resources.jobs;

import java.util.List;
import java.util.Vector;

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
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.util.Timer;

public class GetJobs extends ServerResource {

	/**
	 * Resource to get job status information
	 */

	@Get
	public Representation getJobs() {

		UserSessions sessions = UserSessions.getInstance();
		User user = sessions.getUserByRequest(getRequest());

		if (user != null) {

			JobDao dao = new JobDao();

			String state = "";
			if (getQuery().getFirst("state") != null) {
				state = getQuery().getFirst("state").getValue();
			}

			int limit = 10;

			if (getRequestAttributes().get("limit") != null) {
				limit = Integer.parseInt((String) getRequestAttributes().get(
						"limit"));
			}

			List<AbstractJob> jobs = new Vector<AbstractJob>();

			JsonConfig config = new JsonConfig();

			if (state.equals("running-ltq")) {

				if (!user.isAdmin()) {
					setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
					return new StringRepresentation(
							"The request requires administration rights.");
				}

				config.setExcludes(new String[] { "outputParams",
						"inputParams", "output", "endTime", "startTime",
						"error", "s3Url", "task", "config", "mapReduceJob",
						"job", "step" });
				// all jobs in queue
				jobs = WorkflowEngine.getInstance().getAllJobsInLongTimeQueue();
				for (AbstractJob job : jobs) {

					if (job instanceof CloudgeneJob) {

						((CloudgeneJob) job).updateProgress();

					}
				}
			} else if (state.equals("running-stq")) {

				if (!user.isAdmin()) {
					setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
					return new StringRepresentation(
							"The request requires administration rights.");
				}

				config.setExcludes(new String[] { "outputParams",
						"inputParams", "output", "endTime", "startTime",
						"error", "s3Url", "task", "config", "mapReduceJob",
						"job", "step" });
				// all jobs in queue
				jobs = WorkflowEngine.getInstance()
						.getAllJobsInShortTimeQueue();
				for (AbstractJob job : jobs) {

					if (job instanceof CloudgeneJob) {

						((CloudgeneJob) job).updateProgress();

					}

				}

			} else if (state.equals("current")) {

				if (!user.isAdmin()) {
					setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
					return new StringRepresentation(
							"The request requires administration rights.");
				}

				config.setExcludes(new String[] { "outputParams",
						"inputParams", "output", "endTime", "startTime",
						"error", "s3Url", "task", "config", "mapReduceJob",
						"job", "step" });
				jobs = dao.findAllCurrentJobs(Settings.RETIRED_AFTER_SECS);


			} else if (state.equals("oldjobs")) {

				if (!user.isAdmin()) {
					setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
					return new StringRepresentation(
							"The request requires administration rights.");
				}

				config.setExcludes(new String[] { "outputParams",
						"inputParams", "output", "endTime", "startTime",
						"error", "s3Url", "task", "config", "mapReduceJob",
						"job", "step" });
				jobs = dao.findAllOldJobs(Settings.RETIRED_AFTER_SECS);
				

			} else if (state.equals("retired")) {

				if (!user.isAdmin()) {
					setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
					return new StringRepresentation(
							"The request requires administration rights.");
				}

				config.setExcludes(new String[] { "outputParams",
						"inputParams", "output", "endTime", "startTime",
						"error", "s3Url", "task", "config", "mapReduceJob",
						"job", "step" });
				jobs = dao.findAllRetiredJobs();				

			} else {

				config.setExcludes(new String[] { "user", "outputParams",
						"inputParams", "output", "endTime", "startTime",
						"error", "s3Url", "task", "config", "mapReduceJob",
						"job", "step" });
				// jobs in queue
				jobs = WorkflowEngine.getInstance().getJobsByUser(user);
				for (AbstractJob job : jobs) {

					if (job instanceof CloudgeneJob) {

						((CloudgeneJob) job).updateProgress();

					}

				}

				if (limit > 0) {
					limit = limit - jobs.size();

					// finished jobs
					Timer.start();
					List<AbstractJob> oldJobs = dao.findAllByUser(user, false,
							limit);
					Timer.stop();
					jobs.addAll(oldJobs);

				} else {

					Timer.start();
					List<AbstractJob> oldJobs = dao.findAllByUser(user, false,
							0);
					Timer.stop();
					jobs.addAll(oldJobs);

				}

			}

			JSONArray jsonArray = JSONArray.fromObject(jobs, config);

			return new StringRepresentation(jsonArray.toString());

		} else {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");

		}

	}

}
