package cloudgene.mapred.api.v2.admin.server;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.TemplateDao;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.Template;
import net.sf.json.JSONObject;

public class UpdateTemplate extends BaseResource {

	@Post
	public Representation post(Representation entity) {

		User user = getAuthUser();

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires user authentication.");

		}

		if (!user.isAdmin()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires administration rights.");
		}

		String key = getAttribute("id");

		Form form = new Form(entity);
		String text = form.getFirstValue("text");

		TemplateDao dao = new TemplateDao(getDatabase());
		dao.update(new Template(key, text));

		getWebApp().reloadTemplates();

		return new StringRepresentation("OK.");
	}
	
	@Get
	public Representation get() {

		User user = getAuthUser();

		if (user == null) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires user authentication.");
		}

		if (!user.isAdmin()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires administration rights.");
		}

		TemplateDao dao = new TemplateDao(getDatabase());

		String key = getAttribute("id");
		Template template = dao.findByKey(key);

		if (template == null) {
			return error404("Template " + key + " not found.");
		}

		JSONObject jsonObject = JSONObject.fromObject(template);

		return new StringRepresentation(jsonObject.toString());

	}

}
