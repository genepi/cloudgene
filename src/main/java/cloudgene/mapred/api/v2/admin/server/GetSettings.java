package cloudgene.mapred.api.v2.admin.server;

import java.util.Map;

import cloudgene.mapred.core.User;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.auth.AuthenticationService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import jakarta.inject.Inject;
import net.sf.json.JSONObject;

@Controller
public class GetSettings {

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Get("/api/v2/admin/server/settings")
	@Secured(User.ROLE_ADMIN)
	public String getSettings() {

		JSONObject object = new JSONObject();
		object.put("name", application.getSettings().getName());
		object.put("background-color", application.getSettings().getColors().get("background"));
		object.put("foreground-color", application.getSettings().getColors().get("foreground"));
		object.put("google-analytics", application.getSettings().getGoogleAnalytics());

		Map<String, String> mail = application.getSettings().getMail();
		if (application.getSettings().getMail() != null) {
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

		return object.toString();

	}

}
