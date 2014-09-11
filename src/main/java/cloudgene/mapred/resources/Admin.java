package cloudgene.mapred.resources;

import java.util.HashMap;
import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.ext.freemarker.ContextTemplateLoader;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.util.Template;
import cloudgene.mapred.util.Settings;
import freemarker.template.Configuration;

public class Admin extends ServerResource {

	@Get
	public Representation get() {

		Settings settings = Settings.getInstance();

		Configuration cfg = new Configuration();

		ContextTemplateLoader loader = new ContextTemplateLoader(getContext(),
				"clap://thread/webapp");

		cfg.setTemplateLoader(loader);

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("appname", settings.getName());
		data.put("footer", settings.getTemplate(Template.FOOTER));

		return new TemplateRepresentation("admin.html", cfg, data,
				MediaType.TEXT_HTML);

	}

}
