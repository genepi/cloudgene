package cloudgene.mapred.api.v2.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.JSONConverter;
import cloudgene.mapred.util.PublicUser;
import net.sf.json.JSONObject;

public class CancelJob extends BaseResource {

	private static final Log log = LogFactory.getLog(CancelJob.class);

	@Get
	public Representation get(Representation entity) {

		User user = getAuthUserAndAllowApiToken();

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

		String message = String.format("Job: Canceled job ID %s", job.getId());
		if (user.isAdmin()) {
			message += String.format(" (by ADMIN user ID %s - email %s)", user.getId(), user.getMail());
		}
		log.info(message);

		JSONObject object = JSONConverter.convert(job);

		return new StringRepresentation(object.toString());

	}

}
