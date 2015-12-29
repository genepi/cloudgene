package cloudgene.mapred.resources.jobs;

import java.util.List;
import java.util.Vector;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneParameter;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.util.BaseResource;

public class GetJobDetails extends BaseResource {

	private boolean publicMode = false;
	
	@Post
	protected Representation post(Representation entity, Variant variant) {


		Form form = new Form(entity);
		String jobId = form.getFirstValue("id");

		if (jobId != null) {

			AbstractJob job = getWorkflowEngine().getJobById(jobId);

			if (job == null) {

				JobDao dao = new JobDao(getDatabase());
				job = dao.findById(jobId, true);

			}

			if (job != null) {

				User user = getUser(getRequest());

				//public mode
				if (user == null) {
					UserDao dao = new UserDao(getDatabase());
					user = dao.findByUsername("public");
					publicMode = true; 
				}

				
				if (!user.isAdmin() && job.getUser().getId() != user.getId()) {
					setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
					return new StringRepresentation("Access denied.");
				}

				// finds position in queue
				int position = getWorkflowEngine().getPositionInQueue(job);
				job.setPositionInQueue(position);

				// removes outputs that are for admin only
				List<CloudgeneParameter> adminParams = new Vector<>();
				if (!user.isAdmin()) {
					for (CloudgeneParameter param : job.getOutputParams()) {
						if (param.isAdminOnly()) {
							adminParams.add(param);
						}
					}
				}

				// remove all outputs that are not downloadable
				for (CloudgeneParameter param : job.getOutputParams()) {
					if (!param.isDownload()) {
						adminParams.add(param);
					}
				}
				job.getOutputParams().removeAll(adminParams);

				// excludes properties from json
				JsonConfig config = new JsonConfig();
				config.setExcludes(new String[] { "user", "inputParams",
						"output", "endTime", "startTime", "error", "s3Url",
						"task", "config", "mapReduceJob", "job", "step",
						"context", "hdfsWorkspace", "localWorkspace",
						"logOutFiles", "removeHdfsWorkspace", "settings",
						"setupComplete", "stdOutFile", "workingDirectory",
						"parameter", "logOutFile", "map", "reduce",
						"mapProgress", "reduceProgress", "jobId",
						"makeAbsolute", "mergeOutput", "removeHeader", "value",
						"autoExport", "adminOnly", "download", "tip" });

				if (user.isAdmin()) {
					job.setLogs("logs/" + job.getId());
				}

				JSONObject object = JSONObject.fromObject(job, config);
				if (publicMode){
					object.put("public", publicMode);
				}

				return new StringRepresentation(object.toString(),
						MediaType.APPLICATION_JSON);

			} else {

				setStatus(Status.CLIENT_ERROR_NOT_FOUND);
				return new StringRepresentation("Job " + jobId + " not found.");

			}

		} else {

			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation("Job " + jobId + " not found.");

		}

	}

}
