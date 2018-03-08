package cloudgene.mapred.api.v2.jobs;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
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

	public static final int MAX_PER_PAGE = 25;
	
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

		String page = getQueryValue("page");
		
		int offset = 0;
		if (page != null) {

			offset = Integer.valueOf(page);
			if (offset < 1) {
				offset = 1;
			}
			offset = (offset - 1) * MAX_PER_PAGE;
		}
		
		// find all jobs by user
		JobDao dao = new JobDao(getDatabase());
		
		//count all jobs
		int count = dao.countAllByUser(user);
		
		List<AbstractJob> jobs = null;
		if (page != null) {
			jobs = dao.findAllByUser(user, offset, MAX_PER_PAGE);	
		}else{
			jobs = dao.findAllByUser(user);
		}
		

		// exclude unused parameters
		JsonConfig config = new JsonConfig();
		config.setExcludes(new String[] { "user", "outputParams",
				"inputParams", "output", "error",
				"s3Url", "task", "config", "mapReduceJob", "job", "step",
				"context", "hdfsWorkspace", "localWorkspace", "logOutFiles",
				"logs", "removeHdfsWorkspace", "settings", "setupComplete",
				"stdOutFile", "steps", "workingDirectory", 
				"map", "reduce", "logOutFile", "deletedOn","applicationId","running" });

		JSONObject object = new JSONObject();
		object.put("count", count);

		JSONArray jsonArray = JSONArray.fromObject(jobs, config);
		object.put("data", jsonArray);

		
		return new StringRepresentation(object.toString());

	}
}
