package cloudgene.mapred.api.v2.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.data.MediaType;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.database.DownloadDao;
import cloudgene.mapred.jobs.Download;
import cloudgene.mapred.jobs.workspace.ExternalWorkspaceFactory;
import cloudgene.mapred.util.BaseResource;
import cloudgene.sdk.internal.IExternalWorkspace;
import genepi.io.FileUtil;

public class ShareResults extends BaseResource {
	// TODO: investigate the need for unauthenticated download feature. Whether exposed in UI or not, possibly a valid API endpoint
	private static final Log log = LogFactory.getLog(ShareResults.class);

	@Get
	public Representation get() {

		// FIXME: username is not actually checked
		String username = (String) getRequest().getAttributes().get("username");
		String hash = (String) getRequest().getAttributes().get("hash");
		String filename = (String) getRequest().getAttributes().get("filename");

		DownloadDao dao = new DownloadDao(getDatabase());
		Download download = dao.findByHash(hash);

		if (download == null) {
			return error404("download not found.");
		}

		if (!download.getName().equals(filename)) {
			return error404("download not found.");
		}

		if (download.getCount() == 0) {
			return error400("number of max downloads exceeded.");
		}

		// update download counter if it not set to unlimited
		if (download.getCount() != -1) {
			download.decCount();
			dao.update(download);
		}

		IExternalWorkspace externalWorkspace = ExternalWorkspaceFactory.get(download.getPath());
		if (externalWorkspace != null) {
			// Example scenario: files are stored on an S3 bucket, and this is redirecting the user to a presigned download URL
			String publicUrl = externalWorkspace.createPublicLink(download.getPath());
			redirectTemporary(publicUrl);
			log.info(String.format("Job: Anonymously downloading file '%s' (hash %s) associated with user %s", download.getName(), hash, download.getUsername()));
			return new StringRepresentation(publicUrl);
		} else {
			// no external workspace found, use local workspace
			String localWorkspace = getSettings().getLocalWorkspace();
			String resultFile = FileUtil.path(localWorkspace, download.getPath());
			log.debug("Downloading file from local workspace " + resultFile);
			MediaType mediaType = DownloadResults.getMediaType(download.getPath());

			log.info(String.format("Job: Anonymously downloading file '%s' (hash %s) associated with user %s", download.getName(), hash, download.getUsername()));

			return new FileRepresentation(resultFile, mediaType);
		}

	}

}
