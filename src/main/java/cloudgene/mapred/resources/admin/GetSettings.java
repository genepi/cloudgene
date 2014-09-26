package cloudgene.mapred.resources.admin;

import net.sf.json.JSONObject;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.util.Settings;

public class GetSettings extends ServerResource {

	@Get
	public Representation get() {

		UserSessions sessions = UserSessions.getInstance();
		User user = sessions.getUserByRequest(getRequest());

		if (user != null) {

			if (!user.isAdmin()) {
				setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
				return new StringRepresentation(
						"The request requires administration rights.");
			}

			Settings settings = Settings.getInstance();

			JSONObject object = new JSONObject();
			object.put("name", settings.getName());
			object.put("hadoopPath", settings.getHadoopPath());
			object.put("userApp", settings.getApps().get("user"));
			object.put("adminApp", settings.getApps().get("admin"));
			object.put("mail-smtp", settings.getMail().get("smtp"));
			object.put("mail-port", settings.getMail().get("port"));
			object.put("mail-user", settings.getMail().get("user"));
			object.put("mail-password", settings.getMail().get("password"));
			object.put("mail-name", settings.getMail().get("name"));

			return new StringRepresentation(object.toString());

		} else {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");

		}
	}

}
