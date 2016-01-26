package cloudgene.mapred.resources.jobs;

import genepi.io.FileUtil;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.BaseResource;

public class GetLogs extends BaseResource {

	@Get
	public Representation get() {

		User user = getUser(getRequest());

		if (user == null) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");
		}

		String id = (String) getRequest().getAttributes().get("id");
		String file = (String) getRequest().getAttributes().get("file");
		if (file != null) {
			id += "/" + file;
		}

		JobDao jobDao = new JobDao(getDatabase());
		AbstractJob job = jobDao.findById(id);

		if (job == null) {
			job = getWorkflowEngine().getJobById(id);
		}

		if (job == null) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation("job '" + id + "' not found.");
		}

		if (!user.isAdmin() && job.getUser().getId() != user.getId()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("Access denied.");
		}

		// job.setUser(user);

		StringBuffer buffer = new StringBuffer();

		String workspace = FileUtil.path(getSettings().getLocalWorkspace(), job
				.getUser().getUsername());

		String log = FileUtil.readFileAsString(FileUtil.path(workspace,
				job.getLogOutFile()));
		String output = FileUtil.readFileAsString(FileUtil.path(workspace,
				job.getStdOutFile()));

		buffer.append("<code><pre>");

		if (!log.isEmpty()) {
			buffer.append("job.txt:\n\n");
			buffer.append(log);

		}

		if (!output.isEmpty()) {

			buffer.append("\n\nstd.out:\n\n");
			buffer.append(output);

		}
		buffer.append("</code></pre>");
		return new StringRepresentation(buffer.toString());

	}

}
