package cloudgene.mapred.server.controller;

import java.util.HashMap;
import java.util.Map;

import cloudgene.mapred.server.Application;
import cloudgene.mapred.util.Settings;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.views.View;
import jakarta.inject.Inject;

@Controller
public class PageController {

	@Inject
	protected Application application;

	@Get(uris = { "/index.html", "/", "/start.html" })
	@View("index")
	@Secured(SecurityRule.IS_ANONYMOUS)
	public Map<String, Object> getIndex() {

		Settings settings = application.getSettings();

		Map<String, Object> data = new HashMap<String, Object>();
		String googleAnalytics = settings.getGoogleAnalytics();
		if (googleAnalytics != null && !googleAnalytics.trim().isEmpty()) {
			data.put("google_analytics", googleAnalytics);
		}
		data.put("name", settings.getName());

		// TODO: add more infos (e.g. description, logo etc) and add to head/meta.

		return data;
	}

	@Get("/admin.html")
	@View("admin")
	@Secured(SecurityRule.IS_ANONYMOUS)
	public Map<String, Object> getAdmin() {

		Settings settings = application.getSettings();

		Map<String, Object> data = new HashMap<String, Object>();
		String googleAnalytics = settings.getGoogleAnalytics();
		if (googleAnalytics != null && !googleAnalytics.trim().isEmpty()) {
			data.put("google_analytics", googleAnalytics);
		}
		data.put("name", settings.getName());

		return data;
	}

}
