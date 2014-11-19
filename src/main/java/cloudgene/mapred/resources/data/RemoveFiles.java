package cloudgene.mapred.resources.data;

import genepi.hadoop.HdfsUtil;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.util.BaseResource;

public class RemoveFiles extends BaseResource {

	@Post
	public Representation post(Representation entity) {

		User user = getUser(getRequest());

		StringRepresentation representation = null;

		if (user != null) {

			String workspace = HdfsUtil.path(getSettings().getHdfsWorkspace(), user.getUsername());
			
			Form form = new Form(entity);

			String[] ids = form.getValuesArray("id");
			for (String id : ids) {
				String path = HdfsUtil.path(workspace, id);
				HdfsUtil.delete(path);
			}

			representation = new StringRepresentation("Lukas");
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
