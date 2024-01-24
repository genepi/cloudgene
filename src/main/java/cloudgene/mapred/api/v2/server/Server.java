package cloudgene.mapred.api.v2.server;

import java.util.List;
import java.util.Vector;

import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.apps.ApplicationRepository;
import cloudgene.mapred.core.User;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.Template;
import cloudgene.mapred.wdl.WdlApp;
import net.sf.json.JSONObject;

public class Server extends BaseResource {
	
	@Get
	public Representation getServer() {

		User user = getAuthUserAndAllowApiToken(false);

		JSONObject data = new JSONObject();
		data.put("name", getSettings().getName());
		data.put("background", getSettings().getColors().get("background"));
		data.put("foreground", getSettings().getColors().get("foreground"));
		data.put("footer", getWebApp().getTemplate(Template.FOOTER));
		data.put("emailRequired", getSettings().isEmailRequired());
		data.put("userEmailDescription", getWebApp().getTemplate(Template.USER_EMAIL_DESCRIPTION));
		data.put("userWithoutEmailDescription", getWebApp().getTemplate(Template.USER_WITHOUT_EMAIL_DESCRIPTION));
		if (user != null) {
			JSONObject userJson = new JSONObject();
			userJson.put("username", user.getUsername());
			userJson.put("mail", user.getMail());
			userJson.put("admin", user.isAdmin());
			userJson.put("name", user.getFullName());
			data.put("user", userJson);

			ApplicationRepository repository = getApplicationRepository();
			List<WdlApp> apps = repository.getAllByUser(user);
			data.put("apps", apps);

			List<JSONObject> appsJson = new Vector<JSONObject>();
			List<JSONObject> deprecatedAppsJson = new Vector<JSONObject>();
			List<JSONObject> experimentalAppsJson = new Vector<JSONObject>();


			for (WdlApp app : apps) {
				JSONObject appJson = new JSONObject();
				appJson.put("id", app.getId());
				appJson.put("name", app.getName());
				if (app.getRelease() == null) {
					appsJson.add(appJson);
				} else if (app.getRelease().equals("deprecated")){
					deprecatedAppsJson.add(appJson);
				} else if (app.getRelease().equals("experimental")){
					experimentalAppsJson.add(appJson);
				}else {
					appsJson.add(appJson);
				}
			}

			data.put("apps", appsJson);
			data.put("deprecatedApps", deprecatedAppsJson);
			data.put("experimentalApps", experimentalAppsJson);
			data.put("loggedIn", true);

		} else {
			// get Public apps
			ApplicationRepository repository = getApplicationRepository();
			List<WdlApp> apps = repository.getAllByUser(null);
			data.put("apps", apps);
			data.put("loggedIn", false);
		}

		data.put("navigation", getSettings().getNavigation());
		if (getSettings().isMaintenance()) {
			data.put("maintenace", true);
			data.put("maintenaceMessage", getWebApp().getTemplate(Template.MAINTENANCE_MESSAGE));
		} else {
			data.put("maintenace", false);
		}

		return new StringRepresentation(data.toString());

	}

}
