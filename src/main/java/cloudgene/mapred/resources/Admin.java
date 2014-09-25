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
import org.restlet.resource.ServerResource;

import cloudgene.mapred.WebApp;
import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.util.Template;
import cloudgene.mapred.util.Settings;
import freemarker.template.Configuration;

public class Admin extends ServerResource {

	@Get
	public Representation get() {

		Settings settings = Settings.getInstance();
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

		WebApp app = (WebApp) getApplication();

		Configuration cfg = new Configuration();

		ContextTemplateLoader loader = new ContextTemplateLoader(
				getContext(),
				LocalReference.createFileReference(new File(app.getRootFolder())));

		cfg.setTemplateLoader(loader);

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("appname", settings.getName());
		data.put("footer", settings.getTemplate(Template.FOOTER));

		return new TemplateRepresentation("admin.html", cfg, data,
				MediaType.TEXT_HTML);

	}

}
