package cloudgene.mapred.resources.data;

import genepi.io.FileUtil;
import net.sf.json.JSONArray;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.FileItem;

public class GetLocalFiles extends BaseResource {

	@Post
	public Representation post(Representation entity) {

		User user = getUser(getRequest());

		Form form = new Form(entity);
		String node = form.getFirstValue("node");

		StringRepresentation representation = null;

		if (user != null) {

			String workspace = FileUtil.path(getSettings().getLocalWorkspace(), user.getUsername());

			String rootNode = null;
			if (node.equals("root")) {
				rootNode = "";
			} else {
				rootNode = node;
			}

			FileItem[] items = cloudgene.mapred.util.FileTree.getFileTree(
					workspace, rootNode);
			JSONArray jsonArray = JSONArray.fromObject(items);

			representation = new StringRepresentation(jsonArray.toString());
			getResponse().setStatus(Status.SUCCESS_OK);
			getResponse().setEntity(representation);
			return representation;

		} else {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");

		}

	}

}
