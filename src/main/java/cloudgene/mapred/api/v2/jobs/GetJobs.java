package cloudgene.mapred.api.v2.jobs;

import java.util.List;
import java.util.Vector;

import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.PageUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

public class GetJobs extends BaseResource {

	/**
	 * Resource to get a list of all jobs for authenticated user.
	 */

	public static final int DEFAULT_PAGE_SIZE = 15;

	@Get
	public Representation getJobs() {

		User user = getAuthUserAndAllowApiToken();

		if (user == null) {
			return error401("The request requires user authentication.");
		}

		if (getSettings().isMaintenance() && !user.isAdmin()) {
			return error503("This functionality is currently under maintenance.");
		}

		String page = getQueryValue("page");
		int pageSize = DEFAULT_PAGE_SIZE;

		int offset = 0;
		if (page != null) {

			offset = Integer.valueOf(page);
			if (offset < 1) {
				offset = 1;
			}
			offset = (offset - 1) * pageSize;
		}

		// find all jobs by user
		JobDao dao = new JobDao(getDatabase());

		// count all jobs
		int count = dao.countAllByUser(user);

		List<AbstractJob> jobs = null;
		if (page != null) {
			jobs = dao.findAllByUser(user, offset, pageSize);
		} else {
			jobs = dao.findAllByUser(user);
			page = "1";
			pageSize = count;

		}

		// if job is running, use in memory instance
		List<AbstractJob> finalJobs = new Vector<AbstractJob>();
		for (AbstractJob job : jobs) {
			AbstractJob runningJob = getWorkflowEngine().getJobById(job.getId());
			if (runningJob != null) {
				finalJobs.add(runningJob);
			} else {
				finalJobs.add(job);
			}

		}

		// exclude unused parameters
		JsonConfig config = new JsonConfig();
		config.setExcludes(new String[] { "user", "outputParams", "inputParams", "output", "error", "s3Url", "task",
				"config", "mapReduceJob", "job", "step", "context", "hdfsWorkspace", "localWorkspace", "logOutFiles",
				"logs", "removeHdfsWorkspace", "settings", "setupComplete", "stdOutFile", "steps", "workingDirectory",
				"map", "reduce", "logOutFile", "deletedOn", "applicationId", "running" });

		JSONObject object = PageUtil.createPageObject(Integer.parseInt(page), pageSize, count);

		JSONArray jsonArray = JSONArray.fromObject(finalJobs, config);
		object.put("data", jsonArray);

		return new StringRepresentation(object.toString());
	}

}
