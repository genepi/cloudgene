package cloudgene.mapred.api.v2.jobs;

import cloudgene.mapred.Application;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.util.JSONConverter;
import cloudgene.mapred.util.PublicUser;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import net.sf.json.JSONObject;

@Controller
public class GetJobStatus {

	@Inject
	protected Application application;

	@Get("/api/v2/jobs/{id}/status")
	@Secured(SecurityRule.IS_ANONYMOUS) 
	public String get(String id, @Nullable Authentication authentication) {

		User user = application.getUserByAuthentication(authentication);

		AbstractJob job = application.getWorkflowEngine().getJobById(id);

		if (job == null) {

			JobDao dao = new JobDao(application.getDatabase());
			job = dao.findById(id, false);

		} else {

			if (job instanceof CloudgeneJob) {

				((CloudgeneJob) job).updateProgress();

			}

		}

		if (job == null) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND, "Job " + id + " not found.");
		}

		// public mode
		if (user == null) {
			user = PublicUser.getUser(application.getDatabase());
		}

		if (!user.isAdmin() && job.getUser().getId() != user.getId()) {
			throw new HttpStatusException(HttpStatus.FORBIDDEN, "Access denied.");
		}

		JSONObject object = JSONConverter.convert(job);

		return object.toString();

	}

}
