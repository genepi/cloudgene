package cloudgene.mapred.resources.jobs;

import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;
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
import cloudgene.mapred.util.BaseResource;

public class DeleteJob extends BaseResource {

	@Post
	protected Representation post(Representation entity, Variant variant) {

		User user = getUser(getRequest());

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");

		}

		Form form = new Form(entity);
		String id = form.getFirstValue("id");

		if (id == null) {

			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation("No Job id specified.");

		}

		// delete job from database
		JobDao dao = new JobDao(getDatabase());
		AbstractJob job = dao.findById(id);

		if (!user.isAdmin() && job.getUser().getId() != user.getId()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("Access denied.");
		}

		if (job == null) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation("Job " + id + " not found.");
		}

		String localWorkspace = FileUtil.path(
				getSettings().getLocalWorkspace(), user.getUsername());

		String hdfsWorkspace = HdfsUtil.path(getSettings().getHdfsWorkspace(),
				user.getUsername());

		String localOutput = FileUtil.path(localWorkspace, "output",
				job.getId());

		String hdfsOutput = HdfsUtil.makeAbsolute(HdfsUtil.path(hdfsWorkspace,
				"output", job.getId()));

		String hdfsInput = HdfsUtil.makeAbsolute(HdfsUtil.path(hdfsWorkspace,
				"input", job.getId()));

		FileUtil.deleteDirectory(localOutput);

		HdfsUtil.delete(hdfsOutput);
		HdfsUtil.delete(hdfsInput);

		dao.delete(job);

		JsonConfig config = new JsonConfig();
		config.setExcludes(new String[] { "user", "outputParams",
				"inputParams", "output", "endTime", "startTime", "error",
				"s3Url", "task", "config", "mapReduceJob", "job", "step",
				"context" });
		JSONObject object = JSONObject.fromObject(job, config);

		return new StringRepresentation(object.toString());

	}

}
