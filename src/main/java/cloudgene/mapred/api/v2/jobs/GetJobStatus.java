package cloudgene.mapred.api.v2.jobs;

import net.sf.json.JSONObject;

import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.JSONConverter;

public class GetJobStatus extends BaseResource {
	
	@Get
	public Representation get(Representation entity) {

		User user = getAuthUser();

		String id = getAttribute("job");

		AbstractJob job = getWorkflowEngine().getJobById(id);

		if (job == null) {

			JobDao dao = new JobDao(getDatabase());
			job = dao.findById(id, false);

		} else {

			if (job instanceof CloudgeneJob) {

				((CloudgeneJob) job).updateProgress();

			}

		}

		if (job == null) {
			return error404("Job " + id + " not found.");
		}

		// public mode
		if (user == null) {
			UserDao dao = new UserDao(getDatabase());
			user = dao.findByUsername("public");
		}

		if (!user.isAdmin() && job.getUser().getId() != user.getId()) {
			return error403("Access denied.");
		}

		JSONObject object = JSONConverter.convert(job);

		return new StringRepresentation(object.toString());

	}
	
}
