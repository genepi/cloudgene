package cloudgene.mapred.resources.admin;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.database.TemplateDao;
import cloudgene.mapred.util.Template;
import cloudgene.mapred.util.Settings;

public class UpdateTemplate extends ServerResource {

	@Post
	public Representation post(Representation entity) {

		UserSessions sessions = UserSessions.getInstance();
		User user = sessions.getUserByRequest(getRequest());
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

		Form form = new Form(entity);

		String key = form.getFirstValue("key");
		String text = form.getFirstValue("text");

		TemplateDao dao = new TemplateDao();
		dao.update(new Template(key, text));

		Settings.getInstance().reloadTemplates();

		return new StringRepresentation("OK.");
	}

}
