package cloudgene.mapred.api.v2.jobs;

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
import cloudgene.mapred.util.PublicUser;

public class DeleteJob extends BaseResource {

	@Post
	protected Representation post(Representation entity, Variant variant) {

		User user = getAuthUser();

		if (user == null) {
			user = PublicUser.getUser(getDatabase());
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

		// delete local directory and hdfs directory
		String localOutput = FileUtil.path(getSettings().getLocalWorkspace(),
				job.getId());

		String hdfsOutput = HdfsUtil.makeAbsolute(HdfsUtil.path(getSettings()
				.getHdfsWorkspace(), job.getId()));

		FileUtil.deleteDirectory(localOutput);
		HdfsUtil.delete(hdfsOutput);

		// delete job from database
		dao.delete(job);

		JSONObject object = JSONConverter.fromJob(job);

		return new StringRepresentation(object.toString());

	}

}
