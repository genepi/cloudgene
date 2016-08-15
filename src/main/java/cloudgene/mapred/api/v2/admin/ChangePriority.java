package cloudgene.mapred.api.v2.admin;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.BaseResource;

public class ChangePriority extends BaseResource {

	public static final long HIGH_PRIORITY = 0;

	@Get
	public Representation get() {

		User user = getAuthUser();

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires user authentication.");

		}

		if (!user.isAdmin()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires administration rights.");
		}

		String jobId = getAttribute("job");

		if (jobId == null) {
			return error404("Job " + jobId + " not found.");
		}

		AbstractJob job = getWorkflowEngine().getJobById(jobId);

		if (job == null) {
			return error400("Job " + jobId + " is not running or waiting.");
		}

		getWorkflowEngine().updatePriority(job, HIGH_PRIORITY);

		return new StringRepresentation("Update priority for job " + job.getId() + ".");
	}
}
