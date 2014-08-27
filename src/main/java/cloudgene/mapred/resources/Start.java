package cloudgene.mapred.resources;

import java.util.HashMap;
import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.ContextTemplateLoader;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.util.Settings;
import freemarker.template.Configuration;

public class Start extends ServerResource {

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

		Configuration cfg = new Configuration();

		ContextTemplateLoader loader = new ContextTemplateLoader(getContext(),
				"clap://thread/webapp");

		cfg.setTemplateLoader(loader);

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("appname", settings.getName());
		data.put("username", user.getUsername());
		return new TemplateRepresentation("start.html", cfg, data,
				MediaType.TEXT_HTML);

	}

}
