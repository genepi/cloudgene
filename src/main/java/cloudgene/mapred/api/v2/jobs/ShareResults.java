package cloudgene.mapred.api.v2.jobs;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.database.DownloadDao;
import cloudgene.mapred.jobs.Download;
import cloudgene.mapred.jobs.workspace.ExternalWorkspaceFactory;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.auth.AuthenticationService;
import cloudgene.mapred.server.exceptions.JsonHttpStatusException;
import cloudgene.sdk.internal.IExternalWorkspace;
import genepi.io.FileUtil;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

@Controller
public class ShareResults {

	private static final Log log = LogFactory.getLog(ShareResults.class);

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Get("/share/{username}/{hash}/{filename}")
	@Secured(SecurityRule.IS_ANONYMOUS)
	public HttpResponse<File> get(String username, String hash, String filename) throws URISyntaxException {

		DownloadDao dao = new DownloadDao(application.getDatabase());
		Download download = dao.findByHash(hash);

		if (download == null) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, "download not found.");
		}

		if (!download.getName().equals(filename)) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, "download not found.");
		}

		if (download.getCount() == 0) {
			throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST, "number of max downloads exceeded.");
		}

		// update download counter if it not set to unlimited
		if (download.getCount() != -1) {
			download.decCount();
			dao.update(download);
		}

		IExternalWorkspace externalWorkspace = ExternalWorkspaceFactory.get(download.getPath());
		if (externalWorkspace != null) {
			// external workspace found, use link method and create redirect response
			String publicUrl = externalWorkspace.createPublicLink(download.getPath());
			URI location = new URI(publicUrl);
			return HttpResponse.redirect(location);
		} else {
			// no external workspace found, use local workspace
			String localWorkspace = application.getSettings().getLocalWorkspace();
			String resultFile = FileUtil.path(localWorkspace, download.getPath());
			log.debug("Downloading file from local workspace " + resultFile);
			return HttpResponse.ok(new File(resultFile));
		}

	}

}
