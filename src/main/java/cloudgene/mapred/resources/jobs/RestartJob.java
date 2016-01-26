package cloudgene.mapred.resources.jobs;

import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;
import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.JSONConverter;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlReader;

public class RestartJob extends BaseResource {

	@Post
	public Representation post(Representation entity) {

		User user = getUser(getRequest());

		if (user == null) {
			return error401("The request requires user authentication.");
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
				job.getUser().getUsername());
		String localWorkspace = FileUtil.path(
				getSettings().getLocalWorkspace(), job.getUser().getUsername());

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

		JSONObject object = JSONConverter.fromJob(job);

		return new StringRepresentation(object.toString());

	}

}
