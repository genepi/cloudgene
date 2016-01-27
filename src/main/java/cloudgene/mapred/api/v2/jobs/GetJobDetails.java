package cloudgene.mapred.api.v2.jobs;

import java.util.List;
import java.util.Vector;

import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneParameter;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.JSONConverter;
import cloudgene.mapred.util.PublicUser;

public class GetJobDetails extends BaseResource {

	private boolean publicMode = false;

	@Post
	protected Representation post(Representation entity, Variant variant) {

		User user = getAuthUser();

		
		Form form = new Form(entity);
		String id = form.getFirstValue("id");

		if (id == null) {
			return error404("No job id specified.");
		}

		// running job is in workflow engine
		AbstractJob job = getWorkflowEngine().getJobById(id);

		if (job == null) {
			// finished job is in database
			JobDao dao = new JobDao(getDatabase());
			job = dao.findById(id, true);

		}

		if (job == null) {
			return error404("Job " + id + " not found.");
		}

		// no user, change to public mode
		if (user == null) {
			user = PublicUser.getUser(getDatabase());
			publicMode = true;
		}

		// check permissions
		if (!user.isAdmin() && job.getUser().getId() != user.getId()) {
			return error403("Access denied.");
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

		// set log if user is admin
		if (user.isAdmin()) {
			job.setLogs("logs/" + job.getId());
		}

		JSONObject object = JSONConverter.fromJob(job);
		if (publicMode) {
			object.put("public", publicMode);
		}

		return new StringRepresentation(object.toString(),
				MediaType.APPLICATION_JSON);
	}

}
