package cloudgene.mapred.api.v2.admin.server;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotBlank;

import cloudgene.mapred.Application;
import cloudgene.mapred.auth.AuthenticationService;
import cloudgene.mapred.core.User;
import cloudgene.mapred.exceptions.JsonHttpStatusException;
import cloudgene.mapred.util.Settings;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import net.sf.json.JSONObject;

@Controller
public class UpdateSettings {

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Post("/api/v2/admin/server/settings/update")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public String get(Authentication authentication, String name, String background_color, String foreground_color,
			String google_analytics, String mail, String mail_smtp, String mail_port, String mail_user,
			String mail_password, String mail_name) {

		User user = authenticationService.getUserByAuthentication(authentication);

		if (!user.isAdmin()) {
			throw new JsonHttpStatusException(HttpStatus.UNAUTHORIZED, "The request requires administration rights.");
		}

		Settings settings = application.getSettings();
		settings.setName(name);
		settings.getColors().put("background", background_color);
		settings.getColors().put("foreground", foreground_color);
		settings.setGoogleAnalytics(google_analytics);

		if (mail != null && mail.equals("true")) {
			Map<String, String> mailConfig = new HashMap<String, String>();
			mailConfig.put("smtp", mail_smtp);
			mailConfig.put("port", mail_port);
			mailConfig.put("user", mail_user);
			mailConfig.put("password", mail_password);
			mailConfig.put("name", mail_name);
			application.getSettings().setMail(mailConfig);
		} else {
			application.getSettings().setMail(null);
		}

		application.getSettings().save();

		JSONObject object = new JSONObject();
		object.put("name", application.getSettings().getName());
		object.put("background-color", application.getSettings().getColors().get("background"));
		object.put("foreground-color", application.getSettings().getColors().get("foreground"));
		object.put("google-analytics", application.getSettings().getGoogleAnalytics());

		Map<String, String> mailConfig = application.getSettings().getMail();
		if (application.getSettings().getMail() != null) {
			object.put("mail", true);
			object.put("mail-smtp", mailConfig.get("smtp"));
			object.put("mail-port", mailConfig.get("port"));
			object.put("mail-user", mailConfig.get("user"));
			object.put("mail-password", mailConfig.get("password"));
			object.put("mail-name", mailConfig.get("name"));
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
