package cloudgene.mapred.resources.data;

import net.sf.json.JSONArray;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.FileItem;

public class GetSftpFiles extends BaseResource {

	@Post
	public Representation post(Representation entity) {

		User user = getUser(getRequest());

		Form form = new Form(entity);
		String node = form.getFirstValue("path");
		String username = form.getFirstValue("sftpuser");
		String password = form.getFirstValue("sftppass");
		String host = form.getFirstValue("sftphost");
		int port = Integer.parseInt(form.getFirstValue("sftpport"));

		StringRepresentation representation = null;

		if (user != null) {

			if (node.equals("NOLOAD")) {
				getResponse().setStatus(Status.SUCCESS_OK);
				return representation;
			} else {

				FileItem[] items = null;
				try {
					items = cloudgene.mapred.util.SftpFileTree.getSftpFileTree(
							node, host, username, password, port);
				} catch (Exception e) {
					StringRepresentation error = new StringRepresentation(
							e.getMessage());
					getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
					getResponse().setEntity(error);
					return error;

				}
				JSONArray jsonArray = JSONArray.fromObject(items);
				representation = new StringRepresentation(jsonArray.toString());
				getResponse().setStatus(Status.SUCCESS_OK);
				getResponse().setEntity(representation);
				return representation;
			}

		} else {

			representation = new StringRepresentation("");
			getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
			getResponse().setEntity(representation);
			return representation;

		}

	}

}
