package cloudgene.mapred.server.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import cloudgene.mapred.database.DownloadDao;
import cloudgene.mapred.jobs.Download;
import cloudgene.mapred.jobs.workspace.ExternalWorkspaceFactory;
import cloudgene.mapred.jobs.workspace.IExternalWorkspace;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.exceptions.JsonHttpStatusException;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import jakarta.inject.Inject;

public class DownloadService {

	@Inject
	protected Application application;

	@Inject
	protected ExternalWorkspaceFactory workspaceFactory;

	public MutableHttpResponse<InputStream> download(Download download) throws URISyntaxException, IOException {

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

		IExternalWorkspace externalWorkspace = workspaceFactory.getByUrl(download.getPath());

		// external workspace found, use link method and create redirect response
		String publicUrl = externalWorkspace.createPublicLink(download.getPath());
		if (publicUrl != null) {
			URI location = new URI(publicUrl);
			return HttpResponse.redirect(location);
		} else {
			return HttpResponse.ok(externalWorkspace.download(download.getPath()));
		}
	}

}
