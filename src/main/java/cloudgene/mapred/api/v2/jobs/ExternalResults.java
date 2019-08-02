package cloudgene.mapred.api.v2.jobs;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.ReaderRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.database.DownloadDao;
import cloudgene.mapred.jobs.Download;
import cloudgene.mapred.jobs.workspace.ExternalWorkspaceFactory;
import cloudgene.mapred.jobs.workspace.IExternalWorkspace;
import cloudgene.mapred.util.BaseResource;
import genepi.io.FileUtil;

public class ExternalResults extends BaseResource {

	private static final Log log = LogFactory.getLog(ExternalResults.class);

	@Get
	public Representation get() {

		String hash = (String) getRequest().getAttributes().get("hash");
		String filename = (String) getRequest().getAttributes().get("filename");

		DownloadDao dao = new DownloadDao(getDatabase());
		Download download = dao.findByHash(hash);

		if (download == null) {
			return error404("download not found.");
		}

		if (!download.getName().equals(filename)){
			return error404("download not found.");
		}
		
		if (download.getCount() == 0) {
			return error400("number of max downloads exceeded.");
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

		IExternalWorkspace workspace = ExternalWorkspaceFactory.get(download.getPath());
		
		
		//download.decCount();
		//dao.update(download);


		try {
			return new ReaderRepresentation(new InputStreamReader(workspace.download(download.getPath())));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return error(Status.CLIENT_ERROR_BAD_REQUEST, e.toString());
		}
		
			
		
	}

}
