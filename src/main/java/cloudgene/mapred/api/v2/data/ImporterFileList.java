package cloudgene.mapred.api.v2.data;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class ImporterFileList extends ServerResource {

	@Post
	public Representation validateImport(Representation entity) {

		setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		return new StringRepresentation(
				"URL-based uploads are no longer supported. Please use direct file uploads instead.");

	}

}
