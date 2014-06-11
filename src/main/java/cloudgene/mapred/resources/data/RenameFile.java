package cloudgene.mapred.resources.data;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.util.HdfsUtil;
import cloudgene.mapred.util.Settings;

public class RenameFile extends ServerResource {

	@Post
	public Representation post(Representation entity) {

		UserSessions sessions = UserSessions.getInstance();
		User user = sessions.getUserByRequest(getRequest());

		StringRepresentation representation = null;

		if (user != null) {

			Settings settings = Settings.getInstance();
			String workspace = settings.getHdfsWorkspace(user.getUsername());

			Form form = new Form(entity);

			String parent = form.getFirstValue("parent");
			String id = form.getFirstValue("id");
			String old = form.getFirstValue("old");

			String oldPath = HdfsUtil.path(workspace, parent, old);
			String newPath = HdfsUtil.path(workspace, parent, id);
			HdfsUtil.rename(oldPath, newPath);

			representation = new StringRepresentation("Lukas");
			getResponse().setStatus(Status.SUCCESS_OK);
			getResponse().setEntity(representation);
			return representation;

		} else {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED );
			return new StringRepresentation("The request requires user authentication.");

		}

	}

}
