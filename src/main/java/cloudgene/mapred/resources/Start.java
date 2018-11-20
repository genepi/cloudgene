package cloudgene.mapred.resources;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.ext.freemarker.ContextTemplateLoader;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import cloudgene.mapred.WebApp;
import cloudgene.mapred.util.BaseResource;
import freemarker.template.Configuration;

public class Start extends BaseResource {

	@Get
	public Representation get() {

		WebApp app = (WebApp) getApplication();

		Configuration cfg = new Configuration();

		ContextTemplateLoader loader = new ContextTemplateLoader(getContext(),
				LocalReference.createFileReference(new File(app.getRootFolder())));

		cfg.setTemplateLoader(loader);

		Map<String, Object> data = new HashMap<String, Object>();
		String googleAnalytics = getSettings().getGoogleAnalytics();
		if (googleAnalytics != null && !googleAnalytics.trim().isEmpty()) {
			data.put("google_analytics", googleAnalytics);
		}
		data.put("name", app.getSettings().getName());

		return new TemplateRepresentation("index.html", cfg, data, MediaType.TEXT_HTML);

	}

}
