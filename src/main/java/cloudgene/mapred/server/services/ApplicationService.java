package cloudgene.mapred.server.services;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.apps.Application;
import cloudgene.mapred.apps.ApplicationInstaller;
import cloudgene.mapred.apps.ApplicationRepository;
import cloudgene.mapred.core.User;
import cloudgene.mapred.plugins.PluginManager;
import cloudgene.mapred.plugins.hadoop.HadoopPlugin;
import cloudgene.mapred.server.exceptions.JsonHttpStatusException;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlApp;
import io.micronaut.http.HttpStatus;
import jakarta.inject.Inject;

public class ApplicationService {

	private static final String UNDER_MAINTENANCE = "This functionality is currently under maintenance.";
	private static final String HADOOP_CLUSTER_UNREACHABLE = "Hadoop cluster seems unreachable or misconfigured. Hadoop support is disabled, but this application requires it.";
	private static final String APPLICATION_IS_DATA_PACKAGE = "Application %s is a data package.";
	private static final String APPLICATION_NOT_REMOVED = "Application not removed: %s";
	private static final String APPLICATION_NOT_FOUND = "Application %s not found or the request requires user authentication.";
	private static final String APPLICATION_NOT_UPDATED = "Application not updated: %s";
	private static final String APPLICATION_NOT_INSTALLED = "Application not installed. ";
	private static final String NO_URL = "No url or file location set.";
	private static final String APPLICATION_NOT_INSTALLED_NO_WORKFLOW = "Application not installed: No workflow file found.";

	private static final Log log = LogFactory.getLog(ApplicationService.class);

	@Inject
	protected cloudgene.mapred.server.Application application;

	public Application getById(String appId) {

		ApplicationRepository repository = application.getSettings().getApplicationRepository();
		Application app = repository.getById(appId);

		if (app == null) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, String.format(APPLICATION_NOT_FOUND, appId));
		}

		return app;

	}

	public Application getByIdAndUser(User user, String appId) {

		Settings settings = application.getSettings();

		if (settings.isMaintenance() && (user == null || !user.isAdmin())) {
			throw new JsonHttpStatusException(HttpStatus.SERVICE_UNAVAILABLE, UNDER_MAINTENANCE);
		}

		ApplicationRepository repository = settings.getApplicationRepository();
		Application app = repository.getByIdAndUser(appId, user);

		if (app == null) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, String.format(APPLICATION_NOT_FOUND, appId));
		}

		return app;

	}

	public void checkRequirements(Application app) {

		WdlApp wdlApp = app.getWdlApp();

		if (wdlApp.getWorkflow() == null) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND,
					String.format(APPLICATION_IS_DATA_PACKAGE, app.getId()));
		}

		if (wdlApp.getWorkflow().hasHdfsInputs()) {

			PluginManager manager = PluginManager.getInstance();
			if (!manager.isEnabled(HadoopPlugin.ID)) {
				throw new JsonHttpStatusException(HttpStatus.SERVICE_UNAVAILABLE, HADOOP_CLUSTER_UNREACHABLE);
			}
		}

	}

	public Application removeApp(String appId) {

		ApplicationRepository repository = this.application.getSettings().getApplicationRepository();
		Application app = repository.getById(appId);

		if (app != null) {
			try {
				repository.remove(app);
				this.application.getSettings().save();
				return app;

			} catch (Exception e) {
				e.printStackTrace();
				throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST,
						String.format(APPLICATION_NOT_REMOVED, e.getMessage()));
			}
		} else {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, String.format(APPLICATION_NOT_FOUND, appId));
		}

	}

	public void enableApp(Application app, String enabled) {

		ApplicationRepository repository = this.application.getSettings().getApplicationRepository();

		if (enabled != null) {
			if (app.isEnabled() && enabled.equals("false")) {
				app.setEnabled(false);
				repository.reload();
				this.application.getSettings().save();
			} else if (!app.isEnabled() && enabled.equals("true")) {
				app.setEnabled(true);
				repository.reload();
				this.application.getSettings().save();
			}
		}

	}

	public void updatePermissions(Application app, String permission) {

		ApplicationRepository repository = this.application.getSettings().getApplicationRepository();
		if (app != null) {
			if (permission != null) {
				if (!app.getPermission().equals(permission)) {
					app.setPermission(permission);
					repository.reload();
					this.application.getSettings().save();
				}
			}
		}
	}

	public void updateConfig(Application app, Map<String, String> config) {

		ApplicationRepository repository = this.application.getSettings().getApplicationRepository();
		WdlApp wdlApp = app.getWdlApp();

		if (config != null) {

			Map<String, String> updatedConfig = repository.getConfig(wdlApp);
			updatedConfig.put("nextflow.config", config.get("nextflow.config"));
			updatedConfig.put("nextflow.profile", config.get("nextflow.profile"));
			updatedConfig.put("nextflow.work", config.get("nextflow.work"));
			repository.updateConfig(wdlApp, updatedConfig);
		}
	}

	public void reinstallApp(Application app, String reinstall) {

		ApplicationRepository repository = this.application.getSettings().getApplicationRepository();
		WdlApp wdlApp = app.getWdlApp();

		if (reinstall != null) {
			if (reinstall.equals("true")) {
				boolean installed = ApplicationInstaller.isInstalled(wdlApp, this.application.getSettings());
				if (installed) {
					try {
						ApplicationInstaller.uninstall(wdlApp, this.application.getSettings());
					} catch (IOException e) {
						e.printStackTrace();
						throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST,
								String.format(APPLICATION_NOT_UPDATED, e.getMessage()));
					}
				}
			}
		}

	}

	public List<Application> listApps(String reload) {

		ApplicationRepository repository = application.getSettings().getApplicationRepository();

		if (reload != null && reload.equals("true")) {
			repository.reload();
		}

		List<Application> apps = new Vector<Application>(repository.getAll());
		Collections.sort(apps);

		for (Application app : apps) {
			app.checkForChanges();

		}
		return apps;
	}

	public Application installApp(String url) {

		try {

			if (url == null) {
				throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST, NO_URL);
			}

			ApplicationRepository repository = application.getSettings().getApplicationRepository();

			try {

				Application app = repository.install(url);
				application.getSettings().save();

				if (app != null) {
					return app;
				} else {
					throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST, APPLICATION_NOT_INSTALLED_NO_WORKFLOW);
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.error(APPLICATION_NOT_INSTALLED, e);
				throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST,
						String.format(APPLICATION_NOT_INSTALLED, e.getMessage()));
			}

		} catch (Error e) {
			e.printStackTrace();
			log.error(APPLICATION_NOT_INSTALLED, e);
			throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST,
					String.format(APPLICATION_NOT_INSTALLED, e.getMessage()));
		}

	}

	public ApplicationRepository getRepository() {
		return this.application.getSettings().getApplicationRepository();
	}
}
