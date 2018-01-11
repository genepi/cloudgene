package cloudgene.mapred.api.v2.jobs;

import net.sf.json.JSONObject;

import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.JSONConverter;
import cloudgene.mapred.util.PublicUser;

public class CancelJob extends BaseResource {

	@Get
	public Representation get(Representation entity) {

		User user = getAuthUser();

		if (user == null) {
			user = PublicUser.getUser(getDatabase());
		}

		String id = getAttribute("job");

		if (id == null) {
			return error404("No job id specified.");
		}

		AbstractJob job = getWorkflowEngine().getJobById(id);

		if (job == null) {
			return error404("Job " + id + " not found.");
		}

		if (!user.isAdmin() && job.getUser().getId() != user.getId()) {
			return error403("Access denied.");
		}

		getWorkflowEngine().cancel(job);

		JSONObject object = JSONConverter.convert(job);

		return new StringRepresentation(object.toString());

	}

}
