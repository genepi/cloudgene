package cloudgene.mapred.resources.jobs;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.WorkflowEngine;

public class CancelJob extends ServerResource {

	@Post
	protected Representation post(Representation entity, Variant variant) {

		UserSessions sessions = UserSessions.getInstance();
		User user = sessions.getUserByRequest(getRequest());

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");

		}

		Form form = new Form(entity);
		String id = form.getFirstValue("id");

		if (id != null) {

			AbstractJob job = WorkflowEngine.getInstance().getJobById(id);

			if (job != null) {

				if (!user.isAdmin() && job.getUser().getId() != user.getId()) {
					setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
					return new StringRepresentation("Access denied.");
				}

				WorkflowEngine.getInstance().cancel(job);
				return new StringRepresentation("Job " + id + " canceled.");

			} else {
				setStatus(Status.CLIENT_ERROR_NOT_FOUND);
				return new StringRepresentation("Job " + id + " not found.");
			}

		} else {

			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation("Job " + id + " not found.");

		}

	}

}
