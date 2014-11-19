package cloudgene.mapred.resources.jobs;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.util.BaseResource;

public class NewGetJobStatus extends BaseResource {

	@Post
	protected Representation post(Representation entity, Variant variant) {

		User user = getUser(getRequest());

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");

		}

		Form form = new Form(entity);
		String jobId = form.getFirstValue("job_id");

		AbstractJob job =getWorkflowEngine().getJobById(jobId);

		if (job == null) {

			JobDao dao = new JobDao(getDatabase());
			job = dao.findById(jobId, false);

		} else {

			if (job instanceof CloudgeneJob) {

				((CloudgeneJob) job).updateProgress();

			}

			int position = getWorkflowEngine().getPositionInQueue(job);
			job.setPositionInQueue(position);

		}

		if (job == null) {

			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation("Job " + jobId + " not found.");

		}

		if (!user.isAdmin() && job.getUser().getId() != user.getId()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("Access denied.");
		}

		JsonConfig config = new JsonConfig();
		config.setExcludes(new String[] { "user", "outputParams",
				"inputParams", "output", "endTime", "startTime", "error",
				"s3Url", "task", "config", "mapReduceJob", "job", "step",
				"context" });
		JSONObject object = JSONObject.fromObject(job, config);

		return new StringRepresentation(object.toString());

	} 

}
