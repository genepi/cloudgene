package cloudgene.mapred.api.v2.apps;

import java.util.HashMap;
import java.util.List;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;

import cloudgene.mapred.core.User;
import cloudgene.mapred.util.Application;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.Template;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlHeader;
import cloudgene.mapred.wdl.WdlParameter;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

public class GetApp extends BaseResource {

	@Get
	public Representation getApp() {

		User user = getAuthUser();

		String appId = getAttribute("tool");

		Application application = getSettings().getAppByIdAndUser(appId, user);
		WdlApp wdlApp = null;
		WdlHeader wdlHeader = null;
		try {
			wdlApp = application.getWdlApp();
			wdlHeader = (WdlHeader) wdlApp;
			wdlHeader.setId(appId);

		} catch (Exception e1) {

			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation(
					"Application '" + appId + "' not found or the request requires user authentication..");

		}

		if (getSettings().isMaintenance() && (user == null || !user.isAdmin())) {

			setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
			return new StringRepresentation("This functionality is currently under maintenance.");

		}

		JsonConfig config = new JsonConfig();
		config.setExcludes(new String[] { "mapred", "installed", "cluster" });
		JSONObject jsonObject = JSONObject.fromObject(wdlHeader, config);

		List<WdlParameter> params = wdlApp.getWorkflow().getInputs();
		JSONArray jsonArray = JSONArray.fromObject(params);
		jsonObject.put("params", jsonArray);
		jsonObject.put("submitButton", getWebApp().getTemplate(Template.SUBMIT_BUTTON_TEXT));

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
				return new JsonRepresentation(application);
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				return new StringRepresentation("Application not installed: " + e.getMessage());
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

		String tool = getAttribute("tool");
		Application application = getSettings().getApp(tool);
		if (application != null) {

			try {
				// enable or disable
				if (enabled != null) {
					HashMap<String, String> environment = getSettings().getEnvironment(application);
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

				if (permission != null) {
					if (!application.getPermission().equals(permission)) {
						application.setPermission(permission);
						getSettings().reloadApplications();
						getSettings().save();
					}
				}

				// update permissions

				return new JsonRepresentation(application);

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

}
