package cloudgene.mapred.resources.jobs;

import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;
import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.JSONConverter;

public class DeleteJob extends BaseResource {

	@Post
	protected Representation post(Representation entity, Variant variant) {

		User user = getUser(getRequest());

		if (user == null) {
			return error401("The request requires user authentication.");
		}

		Form form = new Form(entity);
		String id = form.getFirstValue("id");

		if (id == null) {
			return error404("No job id specified.");
		}

		// delete job from database
		JobDao dao = new JobDao(getDatabase());
		AbstractJob job = dao.findById(id);

		if (job == null) {
			return error404("Job " + id + " not found.");
		}
		
		if (!user.isAdmin() && job.getUser().getId() != user.getId()) {
			return error403("Access denied.");
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

		JSONObject object = JSONConverter.fromJob(job);

		return new StringRepresentation(object.toString());

	}

}
