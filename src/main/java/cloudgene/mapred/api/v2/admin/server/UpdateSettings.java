package cloudgene.mapred.api.v2.admin.server;

import java.util.HashMap;
import java.util.Map;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.Settings;
import net.sf.json.JSONObject;

public class UpdateSettings extends BaseResource {

	@Post
	public Representation post(Representation entity) {

		User user = getAuthUser();

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires user authentication.");

		}

		if (!user.isAdmin()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires administration rights.");
		}

		Form form = new Form(entity);
		String name = form.getFirstValue("name");
		String background = form.getFirstValue("background-color");
		String foreground = form.getFirstValue("foreground-color");
		String googleAnalytics = form.getFirstValue("google-analytics");

		String usingMail = form.getFirstValue("mail");
		String mailSmtp = form.getFirstValue("mail-smtp");
		String mailPort = form.getFirstValue("mail-port");
		String mailUser = form.getFirstValue("mail-user");
		String mailPassword = form.getFirstValue("mail-password");
		String mailName = form.getFirstValue("mail-name");

		Settings settings = getSettings();
		settings.setName(name);
		settings.getColors().put("background", background);
		settings.getColors().put("foreground", foreground);
		settings.setGoogleAnalytics(googleAnalytics);

		if (usingMail != null && usingMail.equals("true")) {
			Map<String, String> mail = new HashMap<String, String>();
			mail.put("smtp", mailSmtp);
			mail.put("port", mailPort);
			mail.put("user", mailUser);
			mail.put("password", mailPassword);
			mail.put("name", mailName);
			getSettings().setMail(mail);
		} else {
			getSettings().setMail(null);
		}

		getSettings().save();

		JSONObject object = new JSONObject();
		object.put("name", getSettings().getName());
		object.put("background-color", getSettings().getColors().get("background"));
		object.put("foreground-color", getSettings().getColors().get("foreground"));
		object.put("google-analytics", getSettings().getGoogleAnalytics());

		Map<String, String> mail = getSettings().getMail();
		if (getSettings().getMail() != null) {
			object.put("mail", true);
			object.put("mail-smtp", mail.get("smtp"));
			object.put("mail-port", mail.get("port"));
			object.put("mail-user", mail.get("user"));
			object.put("mail-password", mail.get("password"));
			object.put("mail-name", mail.get("name"));
		} else {
			object.put("mail", false);
			object.put("mail-smtp", "");
			object.put("mail-port", "");
			object.put("mail-user", "");
			object.put("mail-password", "");
			object.put("mail-name", "");
		}

		return new StringRepresentation(object.toString());

	}

}
