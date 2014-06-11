package cloudgene.mapred.resources.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.database.DownloadDao;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.Download;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.util.FileUtil;
import cloudgene.mapred.util.Settings;

public class DownloadResults extends ServerResource {

	private static final Log log = LogFactory.getLog(DownloadResults.class);

	@Get
	public Representation get() {

		UserSessions sessions = UserSessions.getInstance();
		User user = sessions.getUserByRequest(getRequest());

		JobDao jobDao = new JobDao();

		if (user != null) {

			String jobId = (String) getRequest().getAttributes().get("job");
			String id = (String) getRequest().getAttributes().get("id");

			String filename = null;

			if (getRequest().getAttributes().containsKey("filename")) {

				filename = (String) getRequest().getAttributes()
						.get("filename");

			}

			if (getRequest().getAttributes().containsKey("filename2")) {

				jobId = (String) getRequest().getAttributes().get("job") + "/"
						+ (String) getRequest().getAttributes().get("id");
				id = (String) getRequest().getAttributes().get("filename");

				filename = (String) getRequest().getAttributes().get(
						"filename2");

			}

			AbstractJob job = jobDao.findById(jobId);

			if (job == null) {
				job = WorkflowEngine.getInstance().getJobById(jobId);
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

			Settings settings = Settings.getInstance();
			String workspace = settings.getLocalWorkspace(user.getUsername());

			String resultFile = FileUtil.path(workspace, "output", job.getId(),
					id, filename);

			DownloadDao dao = new DownloadDao();
			Download download = dao.findByJobAndPath(jobId,
					FileUtil.path(id, filename));

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

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");

		}

	}

}
