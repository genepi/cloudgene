package cloudgene.mapred.api.v2.jobs;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import cloudgene.mapred.apps.Application;
import cloudgene.mapred.apps.ApplicationRepository;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.PublicUser;
import cloudgene.mapred.wdl.WdlApp;
import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;

public class RestartJob extends BaseResource {
	
	private static final Log log = LogFactory.getLog(RestartJob.class);

	@Get
	public Representation get(Representation entity) {

		User user = getAuthUser();

		if (user == null) {
			user = PublicUser.getUser(getDatabase());
		}

		String id = getAttribute("job");

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

		if (job.getState() == AbstractJob.STATE_DEAD) {

			String hdfsWorkspace = "";			
			try {
				hdfsWorkspace = HdfsUtil.path(getSettings().getHdfsWorkspace(), id);
			} catch (NoClassDefFoundError e) {
				log.warn("Hadoop not found in classpath. Ignore HDFS Workspace.");
			}
			String localWorkspace = FileUtil.path(getSettings().getLocalWorkspace(), id);

			job.setLocalWorkspace(localWorkspace);
			job.setHdfsWorkspace(hdfsWorkspace);
			job.setSettings(getSettings());
			job.setRemoveHdfsWorkspace(getSettings().isRemoveHdfsWorkspace());

			String appId = job.getApplicationId();

			ApplicationRepository repository = getApplicationRepository();
			Application application = repository.getByIdAndUser(appId, job.getUser());
			WdlApp app = null;
			try {
				app = application.getWdlApp();
			} catch (Exception e1) {

				return error400("Application '" + appId + "' not found or the request requires user authentication.");

			}

			((CloudgeneJob) job).loadConfig(app);

			getWorkflowEngine().restart(job);

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("id", id);

			String message = String.format("Job: Restarted job ID %s", job.getId());
			if (user.isAdmin()) {
				message += String.format(" (by ADMIN user ID %s - email %s)", user.getId(), user.getMail());
			}
			log.info(message);

			return ok("Your job was successfully added to the job queue.", params);
		} else {
			return error400("Job " + id + " is not pending.");

		}
	}

}
