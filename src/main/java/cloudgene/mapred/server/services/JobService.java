package cloudgene.mapred.server.services;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloudgene.mapred.apps.ApplicationRepository;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.DownloadDao;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.jobs.CloudgeneParameterOutput;
import cloudgene.mapred.jobs.Download;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.jobs.workspace.IWorkspace;
import cloudgene.mapred.jobs.workspace.WorkspaceFactory;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.exceptions.JsonHttpStatusException;
import cloudgene.mapred.util.FormUtil.Parameter;
import cloudgene.mapred.util.Page;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlParameterInput;
import cloudgene.mapred.wdl.WdlParameterInputType;
import genepi.io.FileUtil;
import io.micronaut.http.HttpStatus;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class JobService {

	private static final Logger log = LoggerFactory.getLogger(JobService.class);

	@Inject
	protected Application application;

	@Inject
	protected WorkspaceFactory workspaceFactory;

	private static final String PARAM_JOB_NAME = "job-name";

	public AbstractJob getById(String id) {

		// TODO: better to go via database? only load from engine when running?

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

		if (user == null) {
			throw new JsonHttpStatusException(HttpStatus.UNAUTHORIZED, "Access denied.");
		}

		AbstractJob job = getById(id);

		// admin has access to all jobs. Other users only to their own jobs.
		if (!user.isAdmin() && job.getUser().getId() != user.getId()) {
			throw new JsonHttpStatusException(HttpStatus.FORBIDDEN, "Access denied.");
		}

		return job;
	}

	public AbstractJob submitJob(String appId, List<Parameter> form, User user) {

		if (user == null) {
			throw new JsonHttpStatusException(HttpStatus.UNAUTHORIZED, "Access denied.");
		}

		WorkflowEngine engine = this.application.getWorkflowEngine();
		Settings settings = this.application.getSettings();

		int maxPerUser = settings.getMaxRunningJobsPerUser();
		if (!user.isAdmin() && engine.getJobsByUser(user).size() >= maxPerUser) {
			throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST,
					"Only " + maxPerUser + " jobs per user can be executed simultaneously.");
		}

		ApplicationRepository repository = settings.getApplicationRepository();
		cloudgene.mapred.apps.Application application = repository.getByIdAndUser(appId, user);
		if (application == null) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, "Application '" + appId + "' not found.");
		}
		WdlApp app = application.getWdlApp();
		if (app.getWorkflow() == null) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND,
					"Application '" + appId + "' has no workflow section.");
		}

		String id = createId();

		Map<String, String> inputParams = null;

		IWorkspace workspace = workspaceFactory.getDefault();

		try {

			// setup workspace
			workspace.setup(id);

			// parse input params
			inputParams = parseAndUpdateInputParams(form, app, workspace);

		} catch (Exception e) {
			throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}

		String name = id;
		String jobName = inputParams.get("job-name");
		if (jobName != null && !jobName.trim().isEmpty()) {
			name = jobName;
		}

		//TODO: remove and solve via workspace!
		String localWorkspace = FileUtil.path(settings.getLocalWorkspace(), id);
		FileUtil.createDirectory(localWorkspace);

		CloudgeneJob job = new CloudgeneJob(user, id, app, inputParams);
		job.setId(id);
		job.setName(name);
		job.setLocalWorkspace(localWorkspace);
		job.setWorkspace(workspace);
		job.setSettings(settings);
		job.setApplication(app.getName() + " " + app.getVersion());
		job.setApplicationId(appId);

		// String userAgent = getRequest().getClientInfo().getAgent();
		// TODO: How to read userAgent from micronaut request!
		String userAgent = "Web.Interface";
		job.setUserAgent(userAgent);

		engine.submit(job);

		return job;

	}

	public Page<AbstractJob> getAllByUserAndPage(User user, Integer page, int pageSize) {

		int offset = 0;
		if (page != null) {

			offset = page;
			if (offset < 1) {
				offset = 1;
			}
			offset = (offset - 1) * pageSize;
		}

		// find all jobs by user
		JobDao dao = new JobDao(application.getDatabase());

		// count all jobs
		int count = dao.countAllByUser(user);

		List<AbstractJob> jobs = null;
		if (page != null) {
			jobs = dao.findAllByUser(user, offset, pageSize);
		} else {
			jobs = dao.findAllByUser(user);
			page = 1;
			pageSize = count;

		}

		// if job is running, use in memory instance
		List<AbstractJob> finalJobs = new Vector<AbstractJob>();
		for (AbstractJob job : jobs) {
			AbstractJob runningJob = application.getWorkflowEngine().getJobById(job.getId());
			if (runningJob != null) {
				finalJobs.add(runningJob);
			} else {
				finalJobs.add(job);
			}

		}

		Page<AbstractJob> result = new Page<AbstractJob>();
		result.setCount(count);
		result.setPage(page);
		result.setPageSize(pageSize);
		result.setData(finalJobs);

		return result;

	}

	public AbstractJob delete(AbstractJob job) {
		Settings settings = application.getSettings();

		// delete local directory
		String localOutput = FileUtil.path(settings.getLocalWorkspace(), job.getId());
		FileUtil.deleteDirectory(localOutput);

		// delete job from database
		job.setState(AbstractJob.STATE_DELETED);

		JobDao dao = new JobDao(application.getDatabase());
		dao.update(job);

		// delete all results that are stored on external workspaces

		IWorkspace workspace = workspaceFactory.getByJob(job);
		try {
			workspace.delete(job.getId());
		} catch (Exception e) {
			log.error("Deleteting " + job.getId() + " form workspace failed.", e);
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

		String localWorkspace = FileUtil.path(settings.getLocalWorkspace(), job.getId());

		job.setLocalWorkspace(localWorkspace);
		job.setSettings(settings);

		String appId = job.getApplicationId();

		ApplicationRepository repository = settings.getApplicationRepository();
		cloudgene.mapred.apps.Application application = repository.getByIdAndUser(appId, job.getUser());
		if (application == null) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, "Application '" + appId + "' not found.");

		}

		
		IWorkspace workspace = workspaceFactory.getDefault();

		try {
			// setup workspace
			workspace.setup(job.getId());
		} catch (Exception e) {
			throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
		job.setWorkspace(workspace);
		
		((CloudgeneJob) job).loadApp(application.getWdlApp());

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

	public AbstractJob changePriority(AbstractJob job, long priority) {
		application.getWorkflowEngine().updatePriority(job, priority);
		return job;
	}

	public String archive(AbstractJob job) {
		Settings settings = application.getSettings();

		JobDao dao = new JobDao(application.getDatabase());

		if (job.getState() != AbstractJob.STATE_SUCCESS && job.getState() != AbstractJob.STATE_FAILED
				&& job.getState() != AbstractJob.STATE_CANCELED) {
			return "Job " + job.getId() + " has wrong state for this operation.";
		}

		try {

			// delete local directory and hdfs directory
			String localOutput = FileUtil.path(settings.getLocalWorkspace(), job.getId());
			FileUtil.deleteDirectory(localOutput);

			job.setState(AbstractJob.STATE_RETIRED);
			dao.update(job);

			IWorkspace workspace = workspaceFactory.getByJob(job);

			try {
				workspace.delete(job.getId());
			} catch (Exception e) {
				log.error("Deleteting " + job.getId() + " from workspace failed.", e);
			}

			return "Retired job " + job.getId();

		} catch (Exception e) {
			return "Retire " + job.getId() + " failed.";
		}

	}

	public String increaseRetireDate(AbstractJob job, int days) {

		JobDao dao = new JobDao(application.getDatabase());
		if (job.getState() == AbstractJob.STATE_SUCESS_AND_NOTIFICATION_SEND
				|| job.getState() == AbstractJob.STATE_FAILED_AND_NOTIFICATION_SEND) {

			try {

				job.setDeletedOn(job.getDeletedOn() + (days * 24 * 60 * 60 * 1000));

				dao.update(job);

				return "Update delete on date for job " + job.getId() + ".";

			} catch (Exception e) {

				return "Update delete date for job " + job.getId() + " failed.";
			}

		} else {
			return "Job " + job.getId() + " has wrong state for this operation.";
		}

	}

	public String createId() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");
		return "job-" + sdf.format(new Date());
	}

	// TODO: refactore and combine this method with CommandLineUtil.parseArgs...

	private Map<String, String> parseAndUpdateInputParams(List<Parameter> form, WdlApp app, IWorkspace workspace)
			throws Exception {

		Map<String, String> props = new HashMap<String, String>();
		Map<String, String> params = new HashMap<String, String>();

		// uploaded files

		for (Parameter formParam : form) {

			String name = formParam.getName();
			Object value = formParam.getValue();

			if (value instanceof File) {
				File inputFile = (File) value;

				try {

					// remove upload indentification!
					String fieldName = name.replace("-upload", "").replace("input-", "");

					WdlParameterInput inputParam = null;
					for (WdlParameterInput input : app.getWorkflow().getInputs()) {
						if (input.getId().equals(fieldName)) {
							inputParam = input;
						}
					}

					if (inputParam.isHdfs()) {

						throw new Exception("HDFS support was removed in Cloudgene 3");

					}

					// copy to workspace in input directory
					String target = workspace.uploadInput(fieldName, inputFile);

					if (inputParam.isFolder()) {
						props.put(fieldName, workspace.getParent(target));
					} else {
						// file
						props.put(fieldName, target);
					}

					// deletes temporary file
					FileUtil.deleteFile(inputFile.getAbsolutePath());

				} catch (Exception e) {
					FileUtil.deleteFile(inputFile.getAbsolutePath());
					throw e;
				}

			} else {

				String key = StringEscapeUtils.escapeHtml(name);
				if (key.startsWith("input-")) {
					key = key.replace("input-", "");
				}

				WdlParameterInput input = getInputParamByName(app, key);

				if (!key.equals(PARAM_JOB_NAME) && !key.endsWith("-pattern") && input == null) {
					throw new Exception("Parameter '" + key + "' not found.");
				}

				String cleanedValue = StringEscapeUtils.escapeHtml(value.toString());

				if (input != null && input.isFileOrFolder() && needsImport(cleanedValue)) {
					throw new Exception("Parameter '" + input.getId()
							+ "': URL-based uploads are no longer supported. Please use direct file uploads instead.");
				}

				if (!props.containsKey(key)) {
					// don't override uploaded files
					props.put(key, cleanedValue);
				}

			}

		}

		for (WdlParameterInput input : app.getWorkflow().getInputs()) {
			if (!params.containsKey(input.getId())) {
				if (props.containsKey(input.getId())) {

					if (input.isFolder() && input.getPattern() != null && !input.getPattern().isEmpty()) {
						String pattern = props.get(input.getId() + "-pattern");
						if (pattern == null) {
							pattern = input.getPattern();
						}
						String value = props.get(input.getId());
						if (!value.endsWith("/")) {
							value = value + "/";
						}
						params.put(input.getId(), value + pattern);
					} else {

						if (input.getTypeAsEnum() == WdlParameterInputType.CHECKBOX) {
							params.put(input.getId(), input.getValues().get("true"));
						} else {
							params.put(input.getId(), props.get(input.getId()));
						}
					}
				} else {
					// ignore invisible input parameters
					if (input.getTypeAsEnum() == WdlParameterInputType.CHECKBOX && input.isVisible()) {
						params.put(input.getId(), input.getValues().get("false"));
					}
				}
			}
		}

		params.put(PARAM_JOB_NAME, props.get(PARAM_JOB_NAME));

		return params;
	}

	private WdlParameterInput getInputParamByName(WdlApp app, String name) {

		for (WdlParameterInput input : app.getWorkflow().getInputs()) {
			if (input.getId().equals(name)) {
				return input;
			}
		}
		return null;
	}

	public List<AbstractJob> getJobs(String state) {

		List<AbstractJob> jobs = new Vector<AbstractJob>();

		WorkflowEngine engine = application.getWorkflowEngine();
		JobDao dao = new JobDao(application.getDatabase());

		if (state != null) {
			switch (state) {

			case "running-ltq":

				jobs = engine.getAllJobsInLongTimeQueue();
				break;

			case "running-stq":

				//TODO: remove!
				jobs = new Vector<AbstractJob>();
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
		return jobs;
	}

	public boolean needsImport(String url) {
		return url.startsWith("sftp://") || url.startsWith("http://") || url.startsWith("https://")
				|| url.startsWith("ftp://") || url.startsWith("s3://");
	}

}
