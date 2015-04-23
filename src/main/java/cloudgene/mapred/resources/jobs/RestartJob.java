package cloudgene.mapred.resources.jobs;

import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlReader;

public class RestartJob extends BaseResource {

	@Post
	public Representation post(Representation entity) {

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

		JobDao dao = new JobDao(getDatabase());
		AbstractJob job = dao.findById(id);

		if (job == null) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation("Job " + id + " not found.");
		}

		if (!user.isAdmin() && job.getUser().getId() != user.getId()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("Access denied.");
		}

		String hdfsWorkspace = HdfsUtil.path(getSettings().getHdfsWorkspace(),
				user.getUsername());
		String localWorkspace = FileUtil.path(
				getSettings().getLocalWorkspace(), user.getUsername());

		job.setLocalWorkspace(localWorkspace);
		job.setHdfsWorkspace(hdfsWorkspace);
		job.setSettings(getSettings());
		job.setRemoveHdfsWorkspace(getSettings().isRemoveHdfsWorkspace());

		String tool = job.getApplicationId();
		String filename = getSettings().getApp(user, tool);
		WdlApp app = null;
		try {
			app = WdlReader.loadAppFromFile(filename);
		} catch (Exception e1) {

			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation("Tool '" + tool + "' not found.");

		}

		((CloudgeneJob) job).loadConfig(app.getMapred());

		getWorkflowEngine().restart(job);

		JsonConfig config = new JsonConfig();
		config.setExcludes(new String[] { "user", "outputParams",
				"inputParams", "output", "endTime", "startTime", "error",
				"s3Url", "task", "config", "mapReduceJob", "job", "step",
				"context" });
		JSONObject object = JSONObject.fromObject(job, config);

		return new StringRepresentation(object.toString());

	}

}
