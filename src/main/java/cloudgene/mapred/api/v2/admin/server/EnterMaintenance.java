package cloudgene.mapred.api.v2.admin.server;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.TemplateDao;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.Template;

public class EnterMaintenance extends BaseResource {

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

		getSettings().setMaintenance(true);

		TemplateDao dao = new TemplateDao(getDatabase());
		dao.update(new Template(
				Template.MAINTENANCE_MESSAGE,
				"Sorry, our service is currently under maintenance. Imputation Server is expected to be down until <b>Tuesday 08:00 AM EDT</b>."));

		getWebApp().reloadTemplates();

		return new StringRepresentation("Enter Maintenance mode.");

	}

}
