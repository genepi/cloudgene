package cloudgene.mapred.api.v2.jobs;

import genepi.io.FileUtil;

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

		User user = getAuthUser();

		if (user == null) {
			return error401("The request requires user authentication.");
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
			return error404("Job " + id + " not found.");
		}


		if (!user.isAdmin() && job.getUser().getId() != user.getId()) {
			return error403("Access denied.");
		}

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
