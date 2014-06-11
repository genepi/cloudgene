package cloudgene.mapred.resources.jobs;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.util.FileUtil;

public class GetLogs extends ServerResource {

	@Get
	public Representation get() {

		UserSessions sessions = UserSessions.getInstance();
		User user = sessions.getUserByRequest(getRequest());

		if (user != null) {
			String id = (String) getRequest().getAttributes().get("id");
			String file = (String) getRequest().getAttributes().get("file");
			if (file != null){
				id += "/" + file;
			}

			JobDao jobDao = new JobDao();
			AbstractJob job = jobDao.findById(id);

			if (job == null){
				job = WorkflowEngine.getInstance().getJobById(id);
			}
			
			if (job != null) {

				if (!user.isAdmin()
						&& job.getUser().getId() != user.getId()) {
					setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
					return new StringRepresentation("Access denied.");
				}
				
				//job.setUser(user);

				StringBuffer buffer = new StringBuffer();

				String log = FileUtil.readFileAsString(job.getLogOutFile());
				String output = FileUtil.readFileAsString(job.getStdOutFile());
				
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

			} else {

				
				
				setStatus(Status.CLIENT_ERROR_UNAUTHORIZED );
				return new StringRepresentation("The request requires user authentication.");
			}

		} else {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED );
			return new StringRepresentation("The request requires user authentication.");

		}

	}

}
