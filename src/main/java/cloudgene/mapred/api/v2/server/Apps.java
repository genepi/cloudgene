package cloudgene.mapred.api.v2.server;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.Environment;
import cloudgene.mapred.util.Application;
import cloudgene.mapred.util.ApplicationInstaller;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.GitHubUtil;
import cloudgene.mapred.util.JSONConverter;
import cloudgene.mapred.util.GitHubUtil.Repository;
import cloudgene.mapred.wdl.WdlApp;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Apps extends BaseResource {

	@Post
	public Representation install(Representation entity) {

		User user = getAuthUser();

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires user authentication.");

		}

		if (!user.isAdmin()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires administration rights.");
		}

		Form form = new Form(entity);
		String id = form.getFirstValue("name");
		String url = form.getFirstValue("url");

		if (url == null) {
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return new StringRepresentation("No url or file location set.");
		}

		// check for unique id
		if (getSettings().getApp(id) != null) {
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return new StringRepresentation("An application with the id '" + id + "' is already installed.");
		}

		try {

			List<Application> applications = new Vector<Application>();

			if (url.startsWith("http://") || url.startsWith("https://")) {
				if (id == null) {
					setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
					return new StringRepresentation("No id set.");
				}
				applications = getSettings().installApplicationFromUrl(id, url);
			} else if (url.startsWith("github://")) {
				String shorthand = url.replaceAll("github://", "");
				Repository repository = GitHubUtil.parseShorthand(shorthand);
				if (repository == null) {
					setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
					return new StringRepresentation(shorthand + " is not a valid GitHub repo.");

				}
				String newId = repository.getUser() + "-" + repository.getRepo();
				if (repository.getDirectory() != null){
					newId += "-" + repository.getDirectory();
				}				
				applications = getSettings().installApplicationFromGitHub(newId, repository, false);
			} else {
				if (id == null) {
					setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
					return new StringRepresentation("No id set.");
				}
				if (url.endsWith(".zip")) {
					applications = getSettings().installApplicationFromZipFile(id, url);
				} else if (url.endsWith(".yaml")) {
					Application application = getSettings().installApplicationFromYaml(id, url);
					if (application != null) {
						applications.add(application);
					}
				} else {
					applications = getSettings().installApplicationFromDirectory(id, url);
				}
			}

			getSettings().save();
			if (applications != null && applications.size() > 0) {
				JSONObject jsonObject = JSONConverter.convert(applications.get(0));
				updateState(applications.get(0), jsonObject);
				return new JsonRepresentation(jsonObject.toString());
			} else {
				setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				return new StringRepresentation("Application not installed: No workflow file found.");
			}

		} catch (Exception e) {
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return new StringRepresentation("Application not installed: " + e.getMessage());
		}

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
		
		String reload = getQueryValue("reload");
		if (reload != null && reload.equals("true")) {
			getSettings().reloadApplications();
		}

		JSONArray jsonArray = new JSONArray();

		List<Application> apps = new Vector<Application>(getSettings().getApps());
		Collections.sort(apps);

		for (Application app : apps) {
			app.checkForChanges();

			JSONObject jsonObject = JSONConverter.convert(app);
			updateState(app, jsonObject);
			jsonArray.add(jsonObject);
		}

		return new StringRepresentation(jsonArray.toString());

	}

	private void updateState(Application app, JSONObject jsonObject) {
		WdlApp wdlApp = app.getWdlApp();
		if (wdlApp != null) {
			if (wdlApp.needsInstallation()) {
				boolean installed = ApplicationInstaller.isInstalled(wdlApp, getSettings());
				if (installed) {
					jsonObject.put("state", "completed");
				} else {
					jsonObject.put("state", "on demand");
				}
			} else {
				jsonObject.put("state", "n/a");
			}
			Map<String, String> environment = Environment.getApplicationVariables(wdlApp, getSettings());
			jsonObject.put("environment", environment);
		}
	}

}
