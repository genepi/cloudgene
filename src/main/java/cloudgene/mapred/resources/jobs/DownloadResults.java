package cloudgene.mapred.resources.jobs;

import genepi.io.FileUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.DownloadDao;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneParameter;
import cloudgene.mapred.jobs.Download;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.util.BaseResource;

public class DownloadResults extends BaseResource {

	private static final Log log = LogFactory.getLog(DownloadResults.class);

	@Get
	public Representation get() {

		String jobId = (String) getRequest().getAttributes().get("job");
		String id = (String) getRequest().getAttributes().get("id");

		String filename = null;

		if (getRequest().getAttributes().containsKey("filename")) {

			filename = (String) getRequest().getAttributes().get("filename");

		}

		if (getRequest().getAttributes().containsKey("filename2")) {

			jobId = (String) getRequest().getAttributes().get("job") + "/"
					+ (String) getRequest().getAttributes().get("id");
			id = (String) getRequest().getAttributes().get("filename");

			filename = (String) getRequest().getAttributes().get("filename2");

		}

		JobDao jobDao = new JobDao(getDatabase());
		AbstractJob job = jobDao.findById(jobId);

		if (job == null) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation("job not found.");

		}

		// job is running -> load it from queue
		if (job.getState() == AbstractJob.STATE_WAITING
				|| job.getState() == AbstractJob.STATE_RUNNING
				|| job.getState() == AbstractJob.STATE_EXPORTING) {
			job = getWorkflowEngine().getJobById(jobId);
		}

		User user = getUser(getRequest());

		// public mode
		if (user == null) {
			UserDao dao = new UserDao(getDatabase());
			user = dao.findByUsername("public");
		}

		if (!user.isAdmin() && job.getUser().getId() != user.getId()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("Access denied.");
		}

		MediaType mediaType = MediaType.ALL;
		if (filename.endsWith(".zip")) {
			mediaType = MediaType.APPLICATION_ZIP;
		} else if (filename.endsWith(".txt") || id.endsWith(".csv")) {
			mediaType = MediaType.TEXT_PLAIN;
		} else if (filename.endsWith(".pdf")) {
			mediaType = MediaType.APPLICATION_PDF;
		} else if (filename.endsWith(".html")) {
			mediaType = MediaType.TEXT_HTML;
		}

		String workspace = FileUtil.path(getSettings().getLocalWorkspace(), job
				.getUser().getUsername());

		DownloadDao dao = new DownloadDao(getDatabase());
		Download download = dao.findByJobAndPath(jobId,
				FileUtil.path(id, filename));

		//job is running and not in database --> download possible of autoexport params
		if (download == null) {
			for (CloudgeneParameter param : job.getOutputParams()) {
				if (param.isAutoExport()) {
					if (param.getFiles() != null) {
						for (Download download2 : param.getFiles()) {
							if (download2.getPath().equals(
									jobId + "/" + FileUtil.path(id, filename))) {
								download = download2;
							}
						}
					}
				}
			}
		}

		if (download != null) {

			String resultFile = FileUtil.path(workspace, "output",
					download.getPath());

			if (download.getCount() > 0) {

				log.debug("Downloading file " + resultFile);

				download.decCount();
				dao.update(download);

				return new FileRepresentation(resultFile, mediaType);

			} else {

				setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				return new StringRepresentation(
						"number of max downloads exceeded.");

			}

		} else {

			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation("download not found.");
		}

	}

}
