package cloudgene.mapred.resources.admin;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.TemplateDao;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.Template;

public class RestartServer extends BaseResource {

	@Get
	public Representation get() {

		User user = getUser(getRequest());

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
		try {
			getWebApp().stop();
			getWebApp().start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new StringRepresentation("Server restarted");

	}

}
