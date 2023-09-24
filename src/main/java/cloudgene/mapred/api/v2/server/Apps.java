package cloudgene.mapred.api.v2.server;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.data.Form;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import cloudgene.mapred.apps.Application;
import cloudgene.mapred.apps.ApplicationInstaller;
import cloudgene.mapred.apps.ApplicationRepository;
import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.Environment;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.JSONConverter;
import cloudgene.mapred.wdl.WdlApp;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Apps extends BaseResource {

	private static final Log log = LogFactory.getLog(Apps.class);

	@Post
	public Representation install(Representation entity) {
		try {
			User user = getAuthUser();

			if (user == null) {
				return error401("The request requires user authentication.");
			}

			if (!user.isAdmin()) {
				return error401("The request requires administration rights.");
			}

			Form form = new Form(entity);
			String url = form.getFirstValue("url");

			if (url == null) {
				return error400("No url or file location set.");
			}

			ApplicationRepository repository = getApplicationRepository();

			try {

				Application application = repository.install(url);

				getSettings().save();

				if (application != null) {
					JSONObject jsonObject = JSONConverter.convert(application);
					updateState(application, jsonObject);
					return new JsonRepresentation(jsonObject.toString());
				} else {
					return error400("Application not installed: No workflow file found.");
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.error("Application not installed. ", e);
				return error400("Application not installed: " + e.getMessage());
			}
		} catch (Error e) {
			e.printStackTrace();
			log.error("Application not installed. ", e);
			return error400("Application not installed: " + e.getMessage());
		}

	}

	@Get
	public Representation get() {

		User user = getAuthUser();

		if (user == null) {
			return error401("The request requires user authentication.");
		}

		if (!user.isAdmin()) {
			return error401("The request requires administration rights.");
		}

		ApplicationRepository repository = getApplicationRepository();

		String reload = getQueryValue("reload");
		if (reload != null && reload.equals("true")) {
			repository.reload();
		}

		JSONArray jsonArray = new JSONArray();

		List<Application> apps = new Vector<Application>(repository.getAll());
		Collections.sort(apps);

		for (Application app : apps) {
			app.checkForChanges();

			JSONObject jsonObject = JSONConverter.convert(app);
			updateState(app, jsonObject);
			
			//read config
			Map<String, String> config = repository.getConfig(app.getWdlApp());
			jsonObject.put("config", config);
			
			jsonArray.add(jsonObject);
		}

		return new StringRepresentation(jsonArray.toString());

	}

	private void updateState(Application app, JSONObject jsonObject) {
		WdlApp wdlApp = app.getWdlApp();
		if (wdlApp != null) {
			if (wdlApp.needsInstallation()) {
				try {
					boolean installed = ApplicationInstaller.isInstalled(wdlApp, getSettings());
					if (installed) {
						jsonObject.put("state", "completed");
					} else {
						jsonObject.put("state", "on demand");
					}
				} catch (NoClassDefFoundError e) {
					// TODO: handle exception
				}
			} else {
				jsonObject.put("state", "n/a");
			}
			Map<String, String> environment = Environment.getApplicationVariables(wdlApp, getSettings());
			jsonObject.put("environment", environment);
			
		}
	}

}
