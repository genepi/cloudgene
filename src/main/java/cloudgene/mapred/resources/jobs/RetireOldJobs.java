package cloudgene.mapred.resources.jobs;

import java.util.List;
import java.util.Vector;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.Settings;

public class RetireOldJobs extends ServerResource {

	@Get
	public Representation post(Representation entity, Variant variant) {

		UserSessions sessions = UserSessions.getInstance();
		User user = sessions.getUserByRequest(getRequest());

		if (user != null) {

			if (!user.isAdmin()) {
				setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
				return new StringRepresentation("Access denied.");
			}

			JobDao dao = new JobDao();

			List<AbstractJob> oldJobs = dao.findAllOldJobs(Settings.RETIRED_AFTER_SECS);

			for (AbstractJob job : oldJobs) {
				job.cleanUp();

				job.setState(AbstractJob.RETIRED);
				dao.update(job);

				List<AbstractJob> jobs = new Vector<AbstractJob>();
				jobs.add(job);

			}

			return new StringRepresentation(oldJobs.size() + " jobs archived!");

		} else {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");

		}

	}

}
