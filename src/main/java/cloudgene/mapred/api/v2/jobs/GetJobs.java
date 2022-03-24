package cloudgene.mapred.api.v2.jobs;

import java.util.List;
import java.util.Vector;

import cloudgene.mapred.Application;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.PageUtil;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

@Controller
public class GetJobs {

	/**
	 * Resource to get a list of all jobs for authenticated user.
	 */

	public static final int DEFAULT_PAGE_SIZE = 15;

	@Inject
	protected Application application;
	
	@Get("/api/v2/jobs")
	@Secured(SecurityRule.IS_AUTHENTICATED) 
	public String getJobs(@Nullable Authentication authentication, @QueryValue @Nullable String page) {

		User user = application.getUserByAuthentication(authentication);

		if (user == null) {
			throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "The request requires user authentication.");
		}

		if (application.getSettings().isMaintenance() && !user.isAdmin()) {
			throw new HttpStatusException(HttpStatus.SERVICE_UNAVAILABLE,
					"This functionality is currently under maintenance.");
		}


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
		JobDao dao = new JobDao(application.getDatabase());

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
			AbstractJob runningJob = application.getWorkflowEngine().getJobById(job.getId());
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

		return object.toString();
	}

}
