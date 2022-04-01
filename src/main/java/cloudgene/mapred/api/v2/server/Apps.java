package cloudgene.mapred.api.v2.server;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.source.doctree.ReturnTree;

import cloudgene.mapred.apps.Application;
import cloudgene.mapred.apps.ApplicationInstaller;
import cloudgene.mapred.apps.ApplicationRepository;
import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.Environment;
import cloudgene.mapred.server.auth.AuthenticationService;
import cloudgene.mapred.server.exceptions.JsonHttpStatusException;
import cloudgene.mapred.server.responses.ApplicationResponse;
import cloudgene.mapred.util.JSONConverter;
import cloudgene.mapred.wdl.WdlApp;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import jakarta.inject.Inject;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
public class Apps {

	private static final Log log = LogFactory.getLog(Apps.class);

	@Inject
	protected cloudgene.mapred.server.Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Post("/api/v2/server/apps")
	@Secured(User.ROLE_ADMIN)
	public ApplicationResponse install(@Nullable String url) {

		try {

			if (url == null) {
				throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST, "No url or file location set.");
			}

			ApplicationRepository repository = application.getSettings().getApplicationRepository();

			try {

				Application app = repository.install(url);

				application.getSettings().save();

				if (application != null) {
					return ApplicationResponse.buildWithDetails(app, application.getSettings(), repository);
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
	@Secured(User.ROLE_ADMIN)
	public List<ApplicationResponse> list(@Nullable @QueryValue("reload") String reload) {

		ApplicationRepository repository = application.getSettings().getApplicationRepository();

		if (reload != null && reload.equals("true")) {
			repository.reload();
		}

		List<Application> apps = new Vector<Application>(repository.getAll());
		Collections.sort(apps);

		for (Application app : apps) {
			app.checkForChanges();

		}
		
		return ApplicationResponse.buildWithDetails(apps, application.getSettings(), repository);

	}

}
