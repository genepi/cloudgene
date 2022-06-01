package cloudgene.mapred.server.controller;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.server.auth.AuthenticationService;
import cloudgene.mapred.server.responses.MessageResponse;
import cloudgene.mapred.server.services.JobCleanUpService;
import cloudgene.mapred.server.services.JobService;
import cloudgene.mapred.util.FormUtil;
import cloudgene.mapred.util.Settings;
import genepi.io.FileUtil;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import jakarta.inject.Inject;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;
import java.util.Vector;

@Controller("/api/v2/admin/jobs")
@Secured(User.ROLE_ADMIN)

public class JobAdminController {

	public static final int DEFAULT_PAGE_SIZE = 15;

	public static final long HIGH_PRIORITY = 0;

	@Inject
	protected cloudgene.mapred.server.Application application;

	@Inject
	protected JobService jobService;

	@Inject
	protected AuthenticationService authenticationService;

	@Inject
	protected JobCleanUpService cleanUpService;

	@Inject
	protected FormUtil formUtil;

	@Get("/{id}/reset")
	public MessageResponse reset(String id, @Nullable @QueryValue("max") Integer max) {

		int maxDownloads = application.getSettings().getMaxDownloads();
		if (max != null) {
			maxDownloads = max;
		}

		AbstractJob job = jobService.getById(id);
		int count = jobService.reset(job, maxDownloads);

		return MessageResponse.success(id + ": counter of " + count + " downloads reset to " + maxDownloads);
	}

	@Get("/{id}/retire")
	public MessageResponse retire(String id) {

		Settings settings = application.getSettings();
		int days = settings.getRetireAfter() - settings.getNotificationAfter();
		AbstractJob job = jobService.getById(id);
		String message = cleanUpService.sendNotification(job, days);
		return MessageResponse.success(message);

	}

	@Get("/{id}/priority")
	public MessageResponse changePriority(String id) {

		AbstractJob job = jobService.getById(id);
		jobService.changePriority(job, HIGH_PRIORITY);
		return MessageResponse.success("Update priority for job " + job.getId() + ".");

	}

	@Get("/{id}/archive")
	public MessageResponse archive(String id) {

		AbstractJob job = jobService.getById(id);
		String message = jobService.archive(job);
		return MessageResponse.success(message);

	}

	@Get("/{id}/change-retire/{days}")
	@Produces(MediaType.TEXT_PLAIN)
	public MessageResponse increaseRetireDate(String id, Integer days) {

		AbstractJob job = jobService.getById(id);
		String message = jobService.increaseRetireDate(job, days);
		return MessageResponse.success(message);

	}

	@Get("/retire")
	@Produces(MediaType.TEXT_PLAIN)
	public String retireJobs() {

		int notifications = cleanUpService.sendNotifications();
		int retired = cleanUpService.executeRetire();

		return "NotificationJob:\n" + notifications + " notifications sent." + "\n\nRetireJob:\n" + retired
				+ " jobs retired.";

	}

	@Get("/")
	public String getJobs(@Nullable @QueryValue("state") String state) {

		WorkflowEngine engine = application.getWorkflowEngine();
		JobDao dao = new JobDao(application.getDatabase());
		List<AbstractJob> jobs = new Vector<AbstractJob>();

		if (state != null) {
			switch (state) {

			case "running-ltq":

				jobs = engine.getAllJobsInLongTimeQueue();
				break;

			case "running-stq":

				jobs = engine.getAllJobsInShortTimeQueue();
				break;

			case "current":

				jobs = dao.findAllNotRetiredJobs();
				List<AbstractJob> toRemove = new Vector<AbstractJob>();
				for (AbstractJob job : jobs) {
					if (engine.isInQueue(job)) {
						toRemove.add(job);
					}
				}
				jobs.removeAll(toRemove);
				break;

			case "retired":

				jobs = dao.findAllByState(AbstractJob.STATE_RETIRED);
				break;

			}
		}

		JsonConfig config = new JsonConfig();
		config.setExcludes(new String[] { "outputParams", "inputParams", "output", "error", "s3Url", "task", "config",
				"mapReduceJob", "job", "step", "context", "hdfsWorkspace", "localWorkspace", "logOutFiles", "logs",
				"removeHdfsWorkspace", "settings", "setupComplete", "stdOutFile", "steps", "workingDirectory",
				"activationCode", "active", "admin", "awsKey", "awsSecretKey", "exportInputToS3", "exportToS3",
				"password", "apiToken", "s3Bucket", "saveCredentials", "map", "reduce", "lastLogin", "loginAttempts",
				"lockedUntil" });

		JSONObject object = new JSONObject();

		int success = 0;
		int failed = 0;
		int pending = 0;
		int waiting = 0;
		int canceld = 0;
		int running = 0;

		for (AbstractJob job : jobs) {

			String workspace = application.getSettings().getLocalWorkspace();
			String folder = FileUtil.path(workspace, job.getId());
			File file = new File(folder);
			if (file.exists()) {
				long size = FileUtils.sizeOfDirectory(file);
				job.setWorkspaceSize(FileUtils.byteCountToDisplaySize(size));
			}

			if (job.getState() == AbstractJob.STATE_EXPORTING || job.getState() == AbstractJob.STATE_RUNNING) {
				running++;
			}
			if (job.getState() == AbstractJob.STATE_SUCCESS
					|| job.getState() == AbstractJob.STATE_SUCESS_AND_NOTIFICATION_SEND) {
				success++;
			}
			if (job.getState() == AbstractJob.STATE_FAILED
					|| job.getState() == AbstractJob.STATE_FAILED_AND_NOTIFICATION_SEND) {
				failed++;
			}
			if (job.getState() == AbstractJob.STATE_DEAD) {
				pending++;
			}
			if (job.getState() == AbstractJob.STATE_WAITING) {
				waiting++;
			}
			if (job.getState() == AbstractJob.STATE_CANCELED) {
				canceld++;
			}
		}

		object.put("count", jobs.size());
		object.put("success", success);
		object.put("failed", failed);
		object.put("pending", pending);
		object.put("waiting", waiting);
		object.put("running", running);
		object.put("canceld", canceld);

		JSONArray jsonArray = JSONArray.fromObject(jobs, config);
		object.put("data", jsonArray);

		return object.toString();

	}

}
