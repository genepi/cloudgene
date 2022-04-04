package cloudgene.mapred.server.services;

import cloudgene.mapred.apps.Application;
import cloudgene.mapred.apps.ApplicationRepository;
import cloudgene.mapred.core.User;
import cloudgene.mapred.plugins.PluginManager;
import cloudgene.mapred.plugins.hadoop.HadoopPlugin;
import cloudgene.mapred.server.exceptions.JsonHttpStatusException;
import cloudgene.mapred.server.responses.ApplicationResponse;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlApp;
import io.micronaut.http.HttpStatus;
import jakarta.inject.Inject;

public class ApplicationService {

	private static final String UNDER_MAINTENANCE = "This functionality is currently under maintenance.";
	private static final String APPLICATION_NOT_FOUND = "Application %s not found or the request requires user authentication.";
	private static final String HADOOP_CLUSTER_UNREACHABLE = "Hadoop cluster seems unreachable or misconfigured. Hadoop support is disabled, but this application requires it.";
	private static final String APPLICATION_IS_DATA_PACKAGE = "Application %s is a data package.";
	private static final String APPLICATION_NOT_REMOVED = "Application not removed: %s";

	@Inject
	protected cloudgene.mapred.server.Application application;

	public Application getbyIdAndUser(User user, String appId) {

		Settings settings = application.getSettings();

		if (settings.isMaintenance() && (user == null || !user.isAdmin())) {
			throw new JsonHttpStatusException(HttpStatus.SERVICE_UNAVAILABLE, UNDER_MAINTENANCE);
		}

		ApplicationRepository repository = settings.getApplicationRepository();
		Application application = repository.getByIdAndUser(appId, user);

		if (application == null) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, String.format(APPLICATION_NOT_FOUND, appId));
		}

		return application;

	}

	public void chcekRequirements(Application app) {

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

}
