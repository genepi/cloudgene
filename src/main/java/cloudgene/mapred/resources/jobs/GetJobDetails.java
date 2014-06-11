package cloudgene.mapred.resources.jobs;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.WorkflowEngine;

public class GetJobDetails extends ServerResource {

	@Post
	protected Representation post(Representation entity, Variant variant) {

		Form form = new Form(entity);

		UserSessions sessions = UserSessions.getInstance();
		User user = sessions.getUserByRequest(getRequest());

		if (user != null) {

			String jobId = form.getFirstValue("id");

			if (jobId != null) {

				AbstractJob job = WorkflowEngine.getInstance().getJobById(jobId);

				if (job == null) {

					JobDao dao = new JobDao();
					job = dao.findById(jobId, true);

				}

				if (job != null) {

					if (!user.isAdmin()
							&& job.getUser().getId() != user.getId()) {
						setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
						return new StringRepresentation("Access denied.");
					}

					JsonConfig config = new JsonConfig();
					config.setExcludes(new String[] { "user", "task",
							"mapReduceJob", "job", "step" });

					JSONObject object = JSONObject.fromObject(job, config);

					return new StringRepresentation(object.toString(),
							MediaType.APPLICATION_JSON);

				} else {

					setStatus(Status.CLIENT_ERROR_NOT_FOUND);
					return new StringRepresentation("Job " + jobId
							+ " not found.");

				}

			} else {

				setStatus(Status.CLIENT_ERROR_NOT_FOUND);
				return new StringRepresentation("Job " + jobId + " not found.");

			}

		} else {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");

		}
	}

}
