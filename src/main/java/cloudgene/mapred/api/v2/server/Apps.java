package cloudgene.mapred.api.v2.server;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.apps.Application;
import cloudgene.mapred.apps.ApplicationInstaller;
import cloudgene.mapred.apps.ApplicationRepository;
import cloudgene.mapred.auth.AuthenticationService;
import cloudgene.mapred.core.User;
import cloudgene.mapred.exceptions.JsonHttpStatusException;
import cloudgene.mapred.jobs.Environment;
import cloudgene.mapred.util.JSONConverter;
import cloudgene.mapred.wdl.WdlApp;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
public class Apps {

	private static final Log log = LogFactory.getLog(Apps.class);

	@Inject
	protected cloudgene.mapred.Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Post("/api/v2/server/apps")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public String install(@Nullable Authentication authentication, @Nullable String url) {

		try {

			User user = authenticationService.getUserByAuthentication(authentication);

			if (!user.isAdmin()) {
				throw new JsonHttpStatusException(HttpStatus.UNAUTHORIZED,
						"The request requires administration rights.");
			}

			if (url == null) {
				throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST, "No url or file location set.");
			}

			ApplicationRepository repository = application.getSettings().getApplicationRepository();

			try {

				Application app = repository.install(url);

				application.getSettings().save();

				if (application != null) {
					JSONObject jsonObject = JSONConverter.convert(app);
					updateState(app, jsonObject);
					return jsonObject.toString();
				} else {
					throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST,
							"Application not installed: No workflow file found.");
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.error("Application not installed. ", e);
				throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST,
						"Application not installed: " + e.getMessage());
			}

		} catch (Error e) {
			e.printStackTrace();
			log.error("Application not installed. ", e);
			throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST, "Application not installed: " + e.getMessage());
		}

	}

	@Get("/api/v2/server/apps")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public String get(Authentication authentication, @Nullable @QueryValue("reload") String reload) {

		User user = authenticationService.getUserByAuthentication(authentication);

		if (!user.isAdmin()) {
			throw new JsonHttpStatusException(HttpStatus.UNAUTHORIZED, "The request requires administration rights.");
		}

		ApplicationRepository repository = application.getSettings().getApplicationRepository();

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

			// read config
			Map<String, String> config = repository.getConfig(app.getWdlApp());
			jsonObject.put("config", config);

			jsonArray.add(jsonObject);
		}

		return jsonArray.toString();

	}

	private void updateState(Application app, JSONObject jsonObject) {
		WdlApp wdlApp = app.getWdlApp();
		if (wdlApp != null) {
			if (wdlApp.needsInstallation()) {
				try {
					boolean installed = ApplicationInstaller.isInstalled(wdlApp, application.getSettings());
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
			Map<String, String> environment = Environment.getApplicationVariables(wdlApp, application.getSettings());
			jsonObject.put("environment", environment);

		}
	}

}
