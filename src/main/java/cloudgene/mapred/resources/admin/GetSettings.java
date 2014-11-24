package cloudgene.mapred.resources.admin;

import net.sf.json.JSONObject;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.util.BaseResource;

public class GetSettings extends BaseResource {

	@Get
	public Representation get() {

		User user = getUser(getRequest());

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");
		}

		if (!user.isAdmin()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires administration rights.");
		}

		JSONObject object = new JSONObject();
		object.put("name", getSettings().getName());
		object.put("hadoopPath", getSettings().getHadoopPath());
		object.put("userApp", getSettings().getApps().get("user").getFilename());
		object.put("adminApp", getSettings().getApps().get("admin").getFilename());
		object.put("mail-smtp", getSettings().getMail().get("smtp"));
		object.put("mail-port", getSettings().getMail().get("port"));
		object.put("mail-user", getSettings().getMail().get("user"));
		object.put("mail-password", getSettings().getMail().get("password"));
		object.put("mail-name", getSettings().getMail().get("name"));
		object.put("piggene", getSettings().getPiggene());

		
		return new StringRepresentation(object.toString());

	}

}
