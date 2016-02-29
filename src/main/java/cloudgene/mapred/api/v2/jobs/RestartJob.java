package cloudgene.mapred.api.v2.jobs;

import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;

import java.util.HashMap;
import java.util.Map;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.PublicUser;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlReader;

public class RestartJob extends BaseResource {

	@Post
	public Representation post(Representation entity) {

		User user = getAuthUser();

		if (user == null) {
			user = PublicUser.getUser(getDatabase());
		}

		Form form = new Form(entity);
		String id = form.getFirstValue("id");

		if (id == null) {
			return error404("No job id specified.");
		}

		JobDao dao = new JobDao(getDatabase());
		AbstractJob job = dao.findById(id);

		if (job == null) {
			return error404("Job " + id + " not found.");
		}

		if (!user.isAdmin() && job.getUser().getId() != user.getId()) {
			return error403("Access denied.");
		}

		String hdfsWorkspace = HdfsUtil.path(getSettings().getHdfsWorkspace(),
				id);
		String localWorkspace = FileUtil.path(
				getSettings().getLocalWorkspace(), id);

		job.setLocalWorkspace(localWorkspace);
		job.setHdfsWorkspace(hdfsWorkspace);
		job.setSettings(getSettings());
		job.setRemoveHdfsWorkspace(getSettings().isRemoveHdfsWorkspace());

		String application = job.getApplicationId();
		String filename = getSettings().getApp(job.getUser(), application);
		WdlApp app = null;
		try {
			app = WdlReader.loadAppFromFile(filename);
		} catch (Exception e1) {

			return error400("Application '"
					+ application
					+ "' not found or the request requires user authentication.");

		}

		((CloudgeneJob) job).loadConfig(app.getMapred());

		getWorkflowEngine().restart(job);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", id);
		return ok("Your job was successfully added to the job queue.", params);

	}

}
