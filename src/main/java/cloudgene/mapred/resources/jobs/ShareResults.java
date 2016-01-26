package cloudgene.mapred.resources.jobs;

import genepi.io.FileUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.data.MediaType;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import cloudgene.mapred.database.DownloadDao;
import cloudgene.mapred.jobs.Download;
import cloudgene.mapred.util.BaseResource;

public class ShareResults extends BaseResource {

	private static final Log log = LogFactory.getLog(ShareResults.class);

	@Get
	public Representation get() {

		String username = (String) getRequest().getAttributes().get("username");
		String hash = (String) getRequest().getAttributes().get("hash");
		String filename = (String) getRequest().getAttributes().get("filename");

		DownloadDao dao = new DownloadDao(getDatabase());
		Download download = dao.findByHash(hash);

		if (download == null) {
			return error400("download not found.");
		}

		MediaType mediaType = MediaType.ALL;
		if (filename.endsWith(".zip")) {
			mediaType = MediaType.APPLICATION_ZIP;
		} else if (filename.endsWith(".txt") || filename.endsWith(".csv")) {
			mediaType = MediaType.TEXT_PLAIN;
		} else if (filename.endsWith(".pdf")) {
			mediaType = MediaType.APPLICATION_PDF;
		} else if (filename.endsWith(".html")) {
			mediaType = MediaType.TEXT_HTML;
		}

		String resultFile = FileUtil.path(getSettings().getLocalWorkspace(),
				download.getPath());

		if (download.getCount() > 0) {

			log.debug("Downloading file " + resultFile);

			download.decCount();
			dao.update(download);

			return new FileRepresentation(resultFile, mediaType);

		} else {

			return error400("number of max downloads exceeded.");

		}

	}

}
