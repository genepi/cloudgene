package cloudgene.mapred.resources;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.ContextTemplateLoader;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.Main;
import cloudgene.mapred.WebApp;
import cloudgene.mapred.core.User;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.Template;
import freemarker.template.Configuration;

public class Admin extends BaseResource {

	@Get
	public Representation get() {

		User user = getAuthUser(false);

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

		WebApp app = getWebApp();

		Configuration cfg = new Configuration();

		ContextTemplateLoader loader = new ContextTemplateLoader(
				getContext(),
				LocalReference.createFileReference(new File(app.getRootFolder())));

		cfg.setTemplateLoader(loader);

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("appname", getSettings().getName());
		data.put("background", getSettings().getColors().get("background"));
		data.put("foreground", getSettings().getColors().get("foreground"));
		data.put("version", Main.VERSION);
		data.put("footer", getWebApp().getTemplate(Template.FOOTER));

		return new TemplateRepresentation("admin.html", cfg, data,
				MediaType.TEXT_HTML);

	}

}
