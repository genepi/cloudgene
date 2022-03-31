package cloudgene.mapred.server.services;

import java.util.List;

import cloudgene.mapred.apps.ApplicationRepository;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.DownloadDao;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.jobs.CloudgeneParameterOutput;
import cloudgene.mapred.jobs.Download;
import cloudgene.mapred.jobs.workspace.ExternalWorkspaceFactory;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.exceptions.JsonHttpStatusException;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.sdk.internal.IExternalWorkspace;
import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;
import io.micronaut.http.HttpStatus;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class JobService {

	@Inject
	protected Application application;

	public AbstractJob getById(String id) {

		AbstractJob job = application.getWorkflowEngine().getJobById(id);

		if (job == null) {
			// finished job is in database
			JobDao dao = new JobDao(application.getDatabase());
			job = dao.findById(id, true);

		} else {

			if (job instanceof CloudgeneJob) {
				((CloudgeneJob) job).updateProgress();
			}
		}

		if (job == null) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, "Job " + id + " not found.");
		}

		return job;
	}

	public AbstractJob getByIdAndUser(String id, User user) {

		AbstractJob job = getById(id);

		// admin has access to all jobs. Other users only to their own jobs.
		if (!user.isAdmin() && job.getUser().getId() != user.getId()) {
			throw new JsonHttpStatusException(HttpStatus.FORBIDDEN, "Access denied.");
		}

		return job;
	}

	public AbstractJob delete(AbstractJob job) {
		Settings settings = application.getSettings();

		// delete local directory and hdfs directory
		String localOutput = FileUtil.path(settings.getLocalWorkspace(), job.getId());
		FileUtil.deleteDirectory(localOutput);

		try {
			String hdfsOutput = HdfsUtil.makeAbsolute(HdfsUtil.path(settings.getHdfsWorkspace(), job.getId()));
			HdfsUtil.delete(hdfsOutput);
		} catch (NoClassDefFoundError e) {
			// ignore HDFS exception to work also without hadoop.
		}

		// delete job from database
		job.setState(AbstractJob.STATE_DELETED);

		JobDao dao = new JobDao(application.getDatabase());
		dao.update(job);

		// delete all results that are stored on external workspaces
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
		return job;
	}

	public AbstractJob cancel(AbstractJob job) {
		application.getWorkflowEngine().cancel(job);
		return job;
	}

	public AbstractJob restart(AbstractJob job) {

		Settings settings = application.getSettings();

		if (job.getState() != AbstractJob.STATE_DEAD) {
			throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST, "Job " + job.getId() + " is not pending.");

		}

		String hdfsWorkspace = "";
		try {
			hdfsWorkspace = HdfsUtil.path(settings.getHdfsWorkspace(), job.getId());
		} catch (NoClassDefFoundError e) {
			// ignore HDFS exception to work also without hadoop.
		}
		String localWorkspace = FileUtil.path(settings.getLocalWorkspace(), job.getId());

		job.setLocalWorkspace(localWorkspace);
		job.setHdfsWorkspace(hdfsWorkspace);
		job.setSettings(settings);
		job.setRemoveHdfsWorkspace(settings.isRemoveHdfsWorkspace());

		String appId = job.getApplicationId();

		ApplicationRepository repository = settings.getApplicationRepository();
		cloudgene.mapred.apps.Application application = repository.getByIdAndUser(appId, job.getUser());
		WdlApp app = null;
		try {
			app = application.getWdlApp();
		} catch (Exception e) {

			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND,
					"Application '" + appId + "' not found or the request requires user authentication.");

		}

		((CloudgeneJob) job).loadConfig(app);

		this.application.getWorkflowEngine().restart(job);

		return job;

	}

	public int reset(AbstractJob job, int maxDownloads) {

		DownloadDao downloadDao = new DownloadDao(application.getDatabase());
		int count = 0;
		for (CloudgeneParameterOutput param : job.getOutputParams()) {
			if (param.isDownload()) {
				List<Download> downloads = param.getFiles();

				for (Download download : downloads) {
					download.setCount(maxDownloads);
					downloadDao.update(download);
					count++;
				}

			}
		}

		return count;

	}

}
