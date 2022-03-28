package cloudgene.mapred.api.v2.admin;

import java.util.List;

import javax.validation.constraints.NotBlank;

import cloudgene.mapred.Application;
import cloudgene.mapred.auth.AuthenticationService;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.DownloadDao;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.exceptions.JsonHttpStatusException;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneParameterOutput;
import cloudgene.mapred.jobs.Download;
import cloudgene.mapred.util.BaseResource;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

@Controller
public class ResetDownloads {

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Get("/api/v2/admin/jobs/{jobId}/reset")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	@Produces(MediaType.TEXT_PLAIN)
	public String get(Authentication authentication, @PathVariable @NotBlank String jobId,
			@Nullable @QueryValue("max") String max) {

		User user = authenticationService.getUserByAuthentication(authentication);

		if (!user.isAdmin()) {
			throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "The request requires administration rights.");
		}

		if (jobId != null) {

			AbstractJob job = application.getWorkflowEngine().getJobById(jobId);

			if (job == null) {

				JobDao dao = new JobDao(application.getDatabase());
				job = dao.findById(jobId, true);

			}

			if (job != null) {

				int maxDownloads = 0;

				if (max != null) {
					maxDownloads = Integer.parseInt(max);
				} else {
					maxDownloads = application.getSettings().getMaxDownloads();
				}

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

				return jobId + ": counter of " + count + " downloads reset to " + maxDownloads;

			}
		}
		throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, "Job " + jobId + " not found.");
	}
}
