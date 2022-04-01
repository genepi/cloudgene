package cloudgene.mapred.api.v2.jobs;

import java.io.File;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.auth.AuthenticationService;
import cloudgene.mapred.server.exceptions.JsonHttpStatusException;
import genepi.io.FileUtil;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

@Controller
public class GetChunk {

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Get("/api/v2/jobs/{jobId}/chunks/{filename}")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public File get(Authentication authentication, String jobId, String filename) {

		JobDao jobDao = new JobDao(application.getDatabase());
		AbstractJob job = jobDao.findById(jobId);

		if (job == null) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, "Job " + jobId + " not found.");
		}

		// job is running -> load it from queue
		if (job.getState() == AbstractJob.STATE_WAITING || job.getState() == AbstractJob.STATE_RUNNING
				|| job.getState() == AbstractJob.STATE_EXPORTING) {
			job = application.getWorkflowEngine().getJobById(jobId);
		}

		User user = authenticationService.getUserByAuthentication(authentication);

		if (!user.isAdmin() && job.getUser().getId() != user.getId()) {
			throw new JsonHttpStatusException(HttpStatus.FORBIDDEN, "Access denied.");
		}

		String resultFile = FileUtil.path(application.getSettings().getLocalWorkspace(), job.getId(), "chunks",
				filename);

		return new File(resultFile);

	}

}
