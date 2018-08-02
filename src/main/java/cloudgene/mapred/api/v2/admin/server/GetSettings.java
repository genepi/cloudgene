package cloudgene.mapred.api.v2.admin.server;

import net.sf.json.JSONObject;

import java.util.Map;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.util.BaseResource;

public class GetSettings extends BaseResource {

	@Get
	public Representation get() {

		User user = getAuthUser();

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires user authentication.");
		}

		if (!user.isAdmin()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires administration rights.");
		}

		JSONObject object = new JSONObject();
		object.put("name", getSettings().getName());
		object.put("background-color", getSettings().getColors().get("background"));
		object.put("foreground-color", getSettings().getColors().get("foreground"));
		object.put("google-analytics", getSettings().getGoogleAnalytics());

		Map<String, String> mail = getSettings().getMail();
		if (getSettings().getMail() != null) {
			object.put("mail", "true");
			object.put("mail-smtp", mail.get("smtp"));
			object.put("mail-port", mail.get("port"));
			object.put("mail-user", mail.get("user"));
			object.put("mail-password", mail.get("password"));
			object.put("mail-name", mail.get("name"));
		} else {
			object.put("mail", "false");
			object.put("mail-smtp", "");
			object.put("mail-port", "");
			object.put("mail-user", "");
			object.put("mail-password", "");
			object.put("mail-name", "");
		}

		return new StringRepresentation(object.toString());

	}

}
