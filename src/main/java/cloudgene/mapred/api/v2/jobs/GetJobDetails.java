package cloudgene.mapred.api.v2.jobs;

import java.util.List;
import java.util.Vector;

import cloudgene.mapred.database.ParameterDao;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneParameterOutput;
import cloudgene.mapred.jobs.workspace.ExternalWorkspaceFactory;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.JSONConverter;
import cloudgene.mapred.util.PublicUser;
import cloudgene.mapred.util.Settings;
import cloudgene.sdk.internal.IExternalWorkspace;
import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;
import net.sf.json.JSONObject;

public class GetJobDetails extends BaseResource {

	private static final Log log = LogFactory.getLog(GetJobDetails.class);

	private boolean publicMode = false;

	@Get
	public Representation get(Representation entity, Variant variant) {

		User user = getAuthUserAndAllowApiToken();

		if (getSettings().isMaintenance() && (user == null || !user.isAdmin())) {
			return error503("This functionality is currently under maintenance.");
		}

		String id = getAttribute("job");

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

		// removes outputs that are for admin only
		List<CloudgeneParameterOutput> adminParams = new Vector<>();
		if (!user.isAdmin()) {
			for (CloudgeneParameterOutput param : job.getOutputParams()) {
				if (param.isAdminOnly()) {
					adminParams.add(param);
				}
			}
		}

		// remove all outputs that are not downloadable
		for (CloudgeneParameterOutput param : job.getOutputParams()) {
			if (!param.isDownload()) {
				adminParams.add(param);
			}
		}
		job.getOutputParams().removeAll(adminParams);

		// set log if user is admin
		if (user.isAdmin()) {
			job.setLogs("logs/" + job.getId());
		}

		JSONObject object = JSONConverter.convert(job);
		if (publicMode) {
			object.put("public", publicMode);
		}

		object.put("username", job.getUser().getUsername());

		return new StringRepresentation(object.toString(), MediaType.APPLICATION_JSON);
	}

	@Delete
	public Representation deleteJob(Representation entity) {

		User user = getAuthUser();

		if (user == null) {
			user = PublicUser.getUser(getDatabase());
		}

		String id = getAttribute("job");

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
		String localOutput = FileUtil.path(getSettings().getLocalWorkspace(), job.getId());

		FileUtil.deleteDirectory(localOutput);

		try {
			String hdfsOutput = HdfsUtil.makeAbsolute(HdfsUtil.path(getSettings().getHdfsWorkspace(), job.getId()));
			HdfsUtil.delete(hdfsOutput);
		} catch (NoClassDefFoundError e) {
			// TODO: handle exception
		}

		// delete job from database
		job.setState(AbstractJob.STATE_DELETED);
		dao.update(job);

		Settings settings = getSettings();

		IExternalWorkspace externalWorkspace = null;
		if (!settings.getExternalWorkspaceLocation().isEmpty()) {
			String externalOutput = settings.getExternalWorkspaceLocation();
			externalWorkspace = ExternalWorkspaceFactory.get(settings.getExternalWorkspaceType(), externalOutput);
			try {
				externalWorkspace.delete(job.getId());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		JSONObject object = JSONConverter.convert(job);

		String message = String.format("Job: Deleted job ID %s", job.getId());
		if (user.isAdmin()) {
			message += String.format(" (by ADMIN user ID %s - email %s)", user.getId(), user.getMail());
		}
		log.info(message);

		return new StringRepresentation(object.toString());

	}

}
