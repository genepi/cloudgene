package cloudgene.mapred.api.v2.server;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.restlet.data.Form;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;

import cloudgene.mapred.apps.Application;
import cloudgene.mapred.apps.ApplicationInstaller;
import cloudgene.mapred.apps.ApplicationRepository;
import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.Environment;
import cloudgene.mapred.plugins.PluginManager;
import cloudgene.mapred.plugins.hadoop.HadoopPlugin;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.JSONConverter;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.util.Template;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlParameterInput;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class App extends BaseResource {

	@Get
	public Representation getApp() {

		User user = getAuthUserAndAllowApiToken();

		String appId = getAttribute("tool");
		try {
			appId = java.net.URLDecoder.decode(appId, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e2) {
			return error404("Application '" + appId + "' is not in valid format.");
		}

		Settings settings = getSettings();

		ApplicationRepository repository = getApplicationRepository();
		Application application = repository.getByIdAndUser(appId, user);

		if (application == null) {
			return error404("Authentication Required: Application '" + appId + "' requires user authentication with an email address. To proceed, please upgrade your profile by entering your email address. You can do this by visiting your profile settings and following the steps provided.");
		}

		WdlApp wdlApp = application.getWdlApp();
		if (wdlApp.getWorkflow() == null) {
			return error404("Application '" + appId + "' is a data package.");
		}

		if (settings.isMaintenance() && (user == null || !user.isAdmin())) {
			return error503("This functionality is currently under maintenance.");
		}

		if (wdlApp.getWorkflow().hasHdfsInputs()) {

			PluginManager manager = PluginManager.getInstance();
			if (!manager.isEnabled(HadoopPlugin.ID)) {
				return error503(
						"Hadoop cluster seems unreachable or misconfigured. Hadoop support is disabled, but this application requires it.");
			}
		}

		List<WdlApp> apps = repository.getAllByUser(user, false);

		JSONObject jsonObject = JSONConverter.convert(application.getWdlApp());

		List<WdlParameterInput> params = wdlApp.getWorkflow().getInputs();
		JSONArray jsonArray = JSONConverter.convert(params, apps);

		jsonObject.put("params", jsonArray);

		jsonObject.put("s3Workspace", settings.getExternalWorkspaceType().equalsIgnoreCase("S3")
				&& settings.getExternalWorkspaceLocation().isEmpty());

		String footer = getWebApp().getTemplate(Template.FOOTER_SUBMIT_JOB);
		if (footer != null && !footer.trim().isEmpty()) {
			jsonObject.put("footer", footer);
		}

		return new StringRepresentation(jsonObject.toString());

	}

	@Delete
	public Representation removeApp() {

		User user = getAuthUser();

		if (user == null) {
			return error401("The request requires user authentication.");
		}

		if (!user.isAdmin()) {
			return error401("The request requires administration rights.");
		}

		String appId = getAttribute("tool");
		try {
			appId = java.net.URLDecoder.decode(appId, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e2) {
			return error404("Application '" + appId + "' is not in valid format.");
		}

		ApplicationRepository repository = getApplicationRepository();
		Application application = repository.getById(appId);
		if (application != null) {
			try {
				repository.remove(application);
				getSettings().save();

				JSONObject jsonObject = JSONConverter.convert(application);
				return new JsonRepresentation(jsonObject.toString());

			} catch (Exception e) {
				e.printStackTrace();
				return error400("Application not removed: " + e.getMessage());
			}
		} else {
			return error404("Application '" + appId + "' not found.");
		}
	}

	@Put
	public Representation updateApp(Representation entity) {

		User user = getAuthUser();

		if (user == null) {
			return error401("The request requires user authentication.");
		}

		if (!user.isAdmin()) {
			return error401("The request requires administration rights.");
		}

		String appId = getAttribute("tool");
		try {
			appId = java.net.URLDecoder.decode(appId, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e2) {
			return error404("Application '" + appId + "' is not in valid format.");
		}

		Form form = new Form(entity);
		String enabled = form.getFirstValue("enabled");
		String permission = form.getFirstValue("permission");
		String reinstall = form.getFirstValue("reinstall");
		String nextflowConfig = form.getFirstValue("config[nextflow.config]");
		String nextflowProfile = form.getFirstValue("config[nextflow.profile]");
		String nextflowWork = form.getFirstValue("config[nextflow.work]");

		ApplicationRepository repository = getApplicationRepository();
		Application application = repository.getById(appId);
		if (application != null) {

			try {
				// enable or disable
				if (enabled != null) {
					if (application.isEnabled() && enabled.equals("false")) {
						application.setEnabled(false);
						repository.reload();
						getSettings().save();
					} else if (!application.isEnabled() && enabled.equals("true")) {
						application.setEnabled(true);
						repository.reload();
						getSettings().save();
					}
				}

				// update permissions
				if (permission != null) {
					if (!application.getPermission().equals(permission)) {
						application.setPermission(permission);
						repository.reload();
						getSettings().save();
					}
				}

				WdlApp wdlApp = application.getWdlApp();

				if (nextflowProfile != null || nextflowConfig != null || nextflowWork != null) {

					Map<String, String> config = repository.getConfig(wdlApp);
					config.put("nextflow.config", nextflowConfig);
					config.put("nextflow.profile", nextflowProfile);
					config.put("nextflow.work", nextflowWork);
					repository.updateConfig(wdlApp, config);
				}

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
				try {
					updateState(application, jsonObject);
				}catch (Error  e ){
					e.printStackTrace();
				}

				// read config
				Map<String, String> config = repository.getConfig(wdlApp);
				jsonObject.put("config", config);

				return new JsonRepresentation(jsonObject.toString());

			} catch (Exception e) {
				e.printStackTrace();
				return error400("Application not installed: " + e.getMessage());
			}

		} else {
			return error404("Application '" + appId + "' not found.");
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
