package cloudgene.mapred.api.v2.admin.server;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.TemplateDao;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.Template;

public class UpdateTemplate extends BaseResource {

	@Post
	public Representation post(Representation entity) {

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

		Form form = new Form(entity);

		String key = form.getFirstValue("key");
		String text = form.getFirstValue("text");

		TemplateDao dao = new TemplateDao(getDatabase());
		dao.update(new Template(key, text));

		getWebApp().reloadTemplates();

		return new StringRepresentation("OK.");
	}

}
