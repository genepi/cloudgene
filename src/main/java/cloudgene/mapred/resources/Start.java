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

import cloudgene.mapred.WebApp;
import cloudgene.mapred.core.User;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.Template;
import freemarker.template.Configuration;

public class Start extends BaseResource {

	@Get
	public Representation get() {

		User user = getUser(getRequest());

		if (user == null) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");
		}

		WebApp app = (WebApp) getApplication();

		Configuration cfg = new Configuration();

		ContextTemplateLoader loader = new ContextTemplateLoader(
				getContext(),
				LocalReference.createFileReference(new File(app.getRootFolder())));

		cfg.setTemplateLoader(loader);

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("appname", getSettings().getName());
		data.put("admin", user.isAdmin());
		data.put("footer", getWebApp().getTemplate(Template.FOOTER));
		data.put("username", user.getUsername());

		if (getSettings().isMaintenance()) {
			data.put("maintenaceMessage",
					getWebApp()
							.getTemplate(Template.MAINTENANCE_MESSAGE));
		}

		return new TemplateRepresentation("start.html", cfg, data,
				MediaType.TEXT_HTML);

	}

}
