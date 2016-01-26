package cloudgene.mapred.resources.jobs;

import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.JSONConverter;

public class CancelJob extends BaseResource {

	@Post
	protected Representation post(Representation entity, Variant variant) {

		User user = getUser(getRequest());

		try {

			if (user == null) {
				return error401("The request requires user authentication.");
			}

			Form form = new Form(entity);
			String id = form.getFirstValue("id");

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

			JSONObject object = JSONConverter.fromJob(job);

			return new StringRepresentation(object.toString());

		} catch (Exception e) {

			return error400(e.getMessage());

		}
	}

}
