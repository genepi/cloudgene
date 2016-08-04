package cloudgene.mapred.api.v2.admin;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.cron.CleanUpTasks;
import cloudgene.mapred.util.BaseResource;

public class RetireJobs extends BaseResource {

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

		return new StringRepresentation("NotificationJob:\n" + notifications
				+ " notifications sent." + "\n\nRetireJob:\n" + retired
				+ " jobs retired.");

	}

}
