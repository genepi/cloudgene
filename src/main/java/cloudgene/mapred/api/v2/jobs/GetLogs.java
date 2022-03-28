package cloudgene.mapred.api.v2.jobs;

import cloudgene.mapred.Application;
import cloudgene.mapred.auth.AuthenticationService;
import cloudgene.mapred.auth.AuthenticationType;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.exceptions.JsonHttpStatusException;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.PublicUser;
import cloudgene.mapred.util.Settings;
import genepi.io.FileUtil;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

@Controller
public class GetLogs {

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Get("/logs/{id}")
	@Secured(SecurityRule.IS_ANONYMOUS)
	public String get(@Nullable Authentication authentication, String id) {

		User user = authenticationService.getUserByAuthentication(authentication, AuthenticationType.ALL_TOKENS);

		if (user == null) {
			user = PublicUser.getUser(application.getDatabase());
		}

		JobDao jobDao = new JobDao(application.getDatabase());
		AbstractJob job = jobDao.findById(id);

		if (job == null) {
			job = application.getWorkflowEngine().getJobById(id);
		}

		if (job == null) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, "Job " + id + " not found.");
		}

		if (!user.isAdmin() && job.getUser().getId() != user.getId()) {
			throw new JsonHttpStatusException(HttpStatus.FORBIDDEN, "Access denied.");
		}

		Settings settings = application.getSettings();
		// log file
		String logFilename = FileUtil.path(settings.getLocalWorkspace(), id, "job.txt");
		String logContent = FileUtil.readFileAsString(logFilename);

		// std out
		String outputFilename = FileUtil.path(settings.getLocalWorkspace(), id, "std.out");
		String outputContent = FileUtil.readFileAsString(outputFilename);

		StringBuffer buffer = new StringBuffer();

		// buffer.append("<code><pre>");

		if (!logContent.isEmpty()) {
			buffer.append("job.txt:\n\n");
			buffer.append(logContent);

		}

		if (!outputContent.isEmpty()) {

			buffer.append("\n\nstd.out:\n\n");
			buffer.append(outputContent);

		}
		// buffer.append("</code></pre>");
		return buffer.toString();

	}

}
