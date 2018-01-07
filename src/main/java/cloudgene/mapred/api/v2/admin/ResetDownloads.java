package cloudgene.mapred.api.v2.admin;

import java.util.List;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.DownloadDao;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.jobs.CloudgeneParameterOutput;
import cloudgene.mapred.jobs.Download;
import cloudgene.mapred.util.BaseResource;

public class ResetDownloads extends BaseResource {

	@Get
	public Representation get() {

		User user = getAuthUser();

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");

		}

		if (!user.isAdmin()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires administration rights.");
		}

		String jobId = getAttribute("job");

		if (jobId != null) {

			AbstractJob job = getWorkflowEngine().getJobById(jobId);

			if (job == null) {

				JobDao dao = new JobDao(getDatabase());
				job = dao.findById(jobId, true);

			}

			if (job != null) {

				DownloadDao downloadDao = new DownloadDao(getDatabase());
				int count = 0;
				for (CloudgeneParameterOutput param : job.getOutputParams()) {
					if (param.isDownload()) {
						List<Download> downloads = param.getFiles();

						for (Download download : downloads) {
							if (download.getCount() < CloudgeneJob.MAX_DOWNLOAD) {
								download.setCount(CloudgeneJob.MAX_DOWNLOAD);
								downloadDao.update(download);
								count++;
							}
						}

					}
				}

				return new StringRepresentation(jobId + ": counter of " + count
						+ " downloads reset to " + CloudgeneJob.MAX_DOWNLOAD);

			}
		}
		setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		return new StringRepresentation("Job not found,.");

	}
}
