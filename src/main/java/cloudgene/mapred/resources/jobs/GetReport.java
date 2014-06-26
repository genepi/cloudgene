package cloudgene.mapred.resources.jobs;

import genepi.io.FileUtil;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.jobs.WorkflowEngine;

public class GetReport extends ServerResource {

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

			if (job == null) {
				job = WorkflowEngine.getInstance().getJobById(id);
			}

			if (job != null) {

				job.setUser(user);

				if (job instanceof CloudgeneJob) {

					CloudgeneJob mapReduceJob = (CloudgeneJob) job;

					StringBuffer buffer = new StringBuffer();

					String log = FileUtil.readFileAsString(mapReduceJob
							.getReportFile());

					if (!log.isEmpty()) {
						buffer.append(log);

					}else{
						return new StringRepresentation("No report file found.");
					}

					return new StringRepresentation(buffer.toString(),MediaType.TEXT_HTML);
				}
				return new StringRepresentation("No report file found.");

			} else {

				setStatus(Status.CLIENT_ERROR_UNAUTHORIZED );
				return new StringRepresentation("The request requires user authentication.");			}

		} else {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED );
			return new StringRepresentation("The request requires user authentication.");

		}

	}

}
