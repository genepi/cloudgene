package cloudgene.mapred.server.services;

import cloudgene.mapred.database.DownloadDao;
import cloudgene.mapred.jobs.Download;
import cloudgene.mapred.jobs.workspace.ExternalWorkspaceFactory;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.exceptions.JsonHttpStatusException;
import cloudgene.sdk.internal.IExternalWorkspace;
import genepi.io.FileUtil;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import jakarta.inject.Inject;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class DownloadService {

	@Inject
	protected Application application;

	public HttpResponse<File> download(Download download) throws URISyntaxException {

		DownloadDao dao = new DownloadDao(application.getDatabase());

		if (download == null) {
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
			return HttpResponse.ok(new File(resultFile));
		}

	}

}
