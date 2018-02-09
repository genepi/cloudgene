package cloudgene.mapred.api.v2.server;

import java.util.List;
import java.util.Map;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;

import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.Environment;
import cloudgene.mapred.util.Application;
import cloudgene.mapred.util.ApplicationInstaller;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.JSONConverter;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.util.Technology;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlParameterInput;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class App extends BaseResource {

	@Get
	public Representation getApp() {

		User user = getAuthUser();

		String appId = getAttribute("tool");

		Settings settings = getSettings();

		Application application = settings.getAppByIdAndUser(appId, user);

		if (application == null) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation(
					"Application '" + appId + "' not found or the request requires user authentication..");
		}

		WdlApp wdlApp = application.getWdlApp();
		if (wdlApp.getWorkflow() == null) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation("Application '" + appId + "' is a data package.");
		}

		if (settings.isMaintenance() && (user == null || !user.isAdmin())) {

			setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
			return new StringRepresentation("This functionality is currently under maintenance.");

		}

		if (wdlApp.getWorkflow().hasHdfsInputs() && !settings.isEnable(Technology.HADOOP_CLUSTER)) {

			setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
			return new StringRepresentation(
					"Hadoop cluster seems unreachable or misconfigured. Hadoop support is disabled, but this application requires it.");

		}

		List<WdlApp> apps = settings.getAppsByUser(user, false);

		JSONObject jsonObject = JSONConverter.convert(application.getWdlApp());

		List<WdlParameterInput> params = wdlApp.getWorkflow().getInputs();
		JSONArray jsonArray = JSONConverter.convert(params, apps);

		jsonObject.put("params", jsonArray);

		return new StringRepresentation(jsonObject.toString());

	}

	@Delete
	public Representation removeApp() {

		User user = getAuthUser();

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires user authentication.");

		}

		if (!user.isAdmin()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires administration rights.");
		}

		String appId = getAttribute("tool");
		Application application = getSettings().getApp(appId);
		if (application != null) {
			try {
				getSettings().deleteApplication(application);
				getSettings().save();

				JSONObject jsonObject = JSONConverter.convert(application);
				return new JsonRepresentation(jsonObject.toString());

			} catch (Exception e) {
				e.printStackTrace();
				setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				return new StringRepresentation("Application not removed: " + e.getMessage());
			}
		} else {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation("Application '" + appId + "' not found.");
		}
	}

	@Put
	public Representation updateApp(Representation entity) {

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
		String enabled = form.getFirstValue("enabled");
		String permission = form.getFirstValue("permission");
		String reinstall = form.getFirstValue("reinstall");

		String tool = getAttribute("tool");
		Application application = getSettings().getApp(tool);
		if (application != null) {

			try {
				// enable or disable
				if (enabled != null) {
					if (application.isEnabled() && enabled.equals("false")) {
						application.setEnabled(false);
						getSettings().reloadApplications();
						getSettings().save();
					} else if (!application.isEnabled() && enabled.equals("true")) {
						application.setEnabled(true);
						getSettings().reloadApplications();
						getSettings().save();
					}
				}

				// update permissions
				if (permission != null) {
					if (!application.getPermission().equals(permission)) {
						application.setPermission(permission);
						getSettings().reloadApplications();
						getSettings().save();
					}
				}

				WdlApp wdlApp = application.getWdlApp();

				// reinstall application
				if (reinstall != null) {
					if (reinstall.equals("true")) {
						boolean installed = ApplicationInstaller.isInstalled(wdlApp, getSettings());
						if (installed) {
							ApplicationInstaller.uninstall(wdlApp, getSettings());
						}
					}
				}

				application.checkForChanges();

				JSONObject jsonObject = JSONConverter.convert(application);
				updateState(application, jsonObject);

				return new JsonRepresentation(jsonObject.toString());

			} catch (Exception e) {
				e.printStackTrace();
				setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				return new StringRepresentation("Application not installed: " + e.getMessage());
			}

		} else {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation("Application '" + tool + "' not found.");
		}
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
