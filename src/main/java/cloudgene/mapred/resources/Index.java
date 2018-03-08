package cloudgene.mapred.resources;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.ext.freemarker.ContextTemplateLoader;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import cloudgene.mapred.WebApp;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.Template;
import cloudgene.mapred.wdl.WdlApp;
import freemarker.template.Configuration;

public class Index extends BaseResource {

	@Get
	public Representation get() {

		WebApp app = (WebApp) getApplication();

		Configuration cfg = new Configuration();

		ContextTemplateLoader loader = new ContextTemplateLoader(getContext(),
				LocalReference.createFileReference(new File(app.getRootFolder())));

		cfg.setTemplateLoader(loader);

		List<WdlApp> apps = getSettings().getAppsByUser(null);

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("appname", getSettings().getName());
		data.put("background", getSettings().getColors().get("background"));
		data.put("foreground", getSettings().getColors().get("foreground"));
		data.put("footer", getWebApp().getTemplate(Template.FOOTER));
		data.put("apps", apps);
		data.put("navigation", getSettings().getNavigation());
		String googleAnalytics = getSettings().getGoogleAnalytics();
		if (googleAnalytics != null && !googleAnalytics.trim().isEmpty()) {
			data.put("google_analytics", googleAnalytics);
		}

		if (getSettings().isMaintenance()) {
			data.put("maintenaceMessage", getWebApp().getTemplate(Template.MAINTENANCE_MESSAGE));
		}

		return new TemplateRepresentation("index.html", cfg, data, MediaType.TEXT_HTML);

	}

}
