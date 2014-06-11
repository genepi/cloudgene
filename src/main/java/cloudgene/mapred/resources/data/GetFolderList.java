package cloudgene.mapred.resources.data;

import net.sf.json.JSONArray;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.util.HdfsItem;
import cloudgene.mapred.util.HdfsTree;
import cloudgene.mapred.util.Settings;

public class GetFolderList extends ServerResource {

	@Post
	public Representation post(Representation entity) {

		UserSessions sessions = UserSessions.getInstance();
		User user = sessions.getUserByRequest(getRequest());

		StringRepresentation representation = null;

		if (user != null) {

			Settings settings = Settings.getInstance();
			String workspace = settings.getHdfsWorkspace(user.getUsername());

			Form form = new Form(entity);
			String node = form.getFirstValue("node");

			String rootNode = null;
			if (node.equals("root")) {
				rootNode = "";
			} else {
				rootNode = node;
			}

			HdfsItem itmes[] = HdfsTree.getFolderTree(workspace, rootNode);
			JSONArray jsonArray = JSONArray.fromObject(itmes);

			representation = new StringRepresentation(jsonArray.toString());
			getResponse().setStatus(Status.SUCCESS_OK);
			getResponse().setEntity(representation);
			return representation;

		} else {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED );
			return new StringRepresentation("The request requires user authentication.");

		}

	}

}
