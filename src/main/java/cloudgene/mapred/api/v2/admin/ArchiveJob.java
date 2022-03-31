package cloudgene.mapred.api.v2.admin;

import javax.validation.constraints.NotBlank;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.workspace.ExternalWorkspaceFactory;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.auth.AuthenticationService;
import cloudgene.mapred.server.exceptions.JsonHttpStatusException;
import cloudgene.mapred.util.Settings;
import cloudgene.sdk.internal.IExternalWorkspace;
import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import jakarta.inject.Inject;

@Controller
public class ArchiveJob {

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Get("/api/v2/admin/jobs/{jobId}/archive")
	@Secured(User.ROLE_ADMIN)	@Produces(MediaType.TEXT_PLAIN)
	public String archive( @PathVariable @NotBlank String jobId) {


		Settings settings = application.getSettings();

		JobDao dao = new JobDao(application.getDatabase());

		AbstractJob job = dao.findById(jobId);

		if (job == null) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, "Job " + jobId + " not found.");
		}

		if (job.getState() == AbstractJob.STATE_SUCCESS || job.getState() == AbstractJob.STATE_FAILED
				|| job.getState() == AbstractJob.STATE_CANCELED) {

			try {

				IExternalWorkspace externalWorkspace = null;
				if (!settings.getExternalWorkspaceLocation().isEmpty()) {
					String externalOutput = settings.getExternalWorkspaceLocation();
					externalWorkspace = ExternalWorkspaceFactory.get(settings.getExternalWorkspaceType(),
							externalOutput);
				}

				// delete local directory and hdfs directory
				String localOutput = FileUtil.path(settings.getLocalWorkspace(), job.getId());
				FileUtil.deleteDirectory(localOutput);
				try {
					String hdfsOutput = HdfsUtil.makeAbsolute(HdfsUtil.path(settings.getHdfsWorkspace(), job.getId()));
					HdfsUtil.delete(hdfsOutput);
				} catch (NoClassDefFoundError e) {
				}
				job.setState(AbstractJob.STATE_RETIRED);
				dao.update(job);

				if (externalWorkspace != null) {
					try {
						externalWorkspace.delete(job.getId());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				return "Retired job " + jobId;

			} catch (Exception e) {
				return "Retire " + job.getId() + " failed.";
			}

		} else {

			return "Job " + jobId + " has wrong state for this operation.";
		}

	}
}
