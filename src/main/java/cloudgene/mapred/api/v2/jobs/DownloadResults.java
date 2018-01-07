package cloudgene.mapred.api.v2.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.DownloadDao;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneParameterOutput;
import cloudgene.mapred.jobs.Download;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.PublicUser;
import genepi.io.FileUtil;

public class DownloadResults extends BaseResource {

	private static final Log log = LogFactory.getLog(DownloadResults.class);

	@Get
	public Representation get() {
		try {
			String jobId = getAttribute("job");
			String paramId = getAttribute("id");
			String filename = getAttribute("filename");

			JobDao jobDao = new JobDao(getDatabase());
			AbstractJob job = jobDao.findById(jobId);

			if (job == null) {
				return error404("Job " + jobId + " not found.");
			}

			// job is running -> load it from queue
			if (job.getState() == AbstractJob.STATE_WAITING || job.getState() == AbstractJob.STATE_RUNNING
					|| job.getState() == AbstractJob.STATE_EXPORTING) {
				job = getWorkflowEngine().getJobById(jobId);
			}

			User user = getAuthUser(false);

			// public mode
			if (user == null) {
				user = PublicUser.getUser(getDatabase());
			}

			if (!user.isAdmin() && job.getUser().getId() != user.getId()) {
				return error403("Access denied.");
			}

			MediaType mediaType = MediaType.ALL;
			if (filename.endsWith(".zip")) {
				mediaType = MediaType.APPLICATION_ZIP;
			} else if (filename.endsWith(".txt") || filename.endsWith(".csv")) {
				mediaType = MediaType.TEXT_PLAIN;
			} else if (filename.endsWith(".pdf")) {
				mediaType = MediaType.APPLICATION_PDF;
			} else if (filename.endsWith(".html")) {
				mediaType = MediaType.TEXT_HTML;
			}

			DownloadDao dao = new DownloadDao(getDatabase());
			Download download = dao.findByJobAndPath(jobId, FileUtil.path(paramId, filename));

			// job is running and not in database --> download possible of
			// autoexport params
			if (download == null) {
				for (CloudgeneParameterOutput param : job.getOutputParams()) {
					if (param.isAutoExport()) {
						if (param.getFiles() != null) {
							for (Download download2 : param.getFiles()) {
								if (download2.getPath().equals(jobId + "/" + FileUtil.path(paramId, filename))) {
									download = download2;
								}
							}
						}
					}
				}
			}

			if (download == null) {
				return error404("download not found.");
			}

			if (download.getCount() == 0) {
				return error400("number of max downloads exceeded.");
			}

			String resultFile = FileUtil.path(getSettings().getLocalWorkspace(), download.getPath());

			log.debug("Downloading file " + resultFile);

			// update download counter
			download.decCount();
			dao.update(download);
			return new FileRepresentation(resultFile, mediaType);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return error(Status.CLIENT_ERROR_BAD_REQUEST, "oje");
	}

}
