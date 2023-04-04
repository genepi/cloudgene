package cloudgene.mapred.api.v2.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.cron.CleanUpTasks;
import cloudgene.mapred.util.BaseResource;

public class RetireJobs extends BaseResource {
	private static final Log log = LogFactory.getLog(RetireJobs.class);

	@Get
	public Representation get() {

		User user = getAuthUser();

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");

		}

		if (!user.isAdmin()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires administration rights.");
		}

		int notifications = CleanUpTasks.sendNotifications(getWebApp());
		int retired = CleanUpTasks.executeRetire(getDatabase(), getSettings());

		log.info(String.format("Job: Manually triggered retiring of all eligible jobs (by ADMIN user ID %s - email %s)", user.getId(), user.getMail()));

		return new StringRepresentation("NotificationJob:\n" + notifications
				+ " notifications sent." + "\n\nRetireJob:\n" + retired
				+ " jobs retired.");

	}

}
