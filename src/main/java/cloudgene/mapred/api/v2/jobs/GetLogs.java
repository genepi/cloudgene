package cloudgene.mapred.api.v2.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.PublicUser;
import cloudgene.mapred.util.Settings;
import genepi.io.FileUtil;

public class GetLogs extends BaseResource {

	private static final Log log = LogFactory.getLog(GetLogs.class);

	@Get
	public Representation get() {

		User user = getAuthUserAndAllowApiToken(false);

		if (user == null) {
			user = PublicUser.getUser(getDatabase());
		}

		String id = getAttribute("id");

		JobDao jobDao = new JobDao(getDatabase());
		AbstractJob job = jobDao.findById(id);

		if (job == null) {
			job = getWorkflowEngine().getJobById(id);
		}

		if (job == null) {
			return error404("Job " + id + " not found.");
		}

		if (!user.isAdmin() && job.getUser().getId() != user.getId()) {
			return error403("Access denied.");
		}

		Settings settings = getSettings();
		// log file
		String logFilename = FileUtil.path(settings.getLocalWorkspace(), id,
				"job.txt");
		String logContent = FileUtil.readFileAsString(logFilename);

		// std out
		String outputFilename = FileUtil.path(settings.getLocalWorkspace(), id,
				"std.out");
		String outputContent = FileUtil.readFileAsString(outputFilename);

		StringBuffer buffer = new StringBuffer();

		//buffer.append("<code><pre>");

		if (!logContent.isEmpty()) {
			buffer.append("job.txt:\n\n");
			buffer.append(logContent);

		}

		if (!outputContent.isEmpty()) {

			buffer.append("\n\nstd.out:\n\n");
			buffer.append(outputContent);

		}

		String message = String.format("Job: viewing logs for job ID %s", job.getId());
		if (user.isAdmin()) {
			message += String.format(" (by ADMIN user ID %s - email %s)", user.getId(), user.getMail());
		}
		log.info(message);
		//buffer.append("</code></pre>");
		return new StringRepresentation(buffer.toString());

	}

}
