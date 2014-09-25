package cloudgene.mapred.resources.apps;

import java.io.IOException;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.util.Template;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlHeader;
import cloudgene.mapred.wdl.WdlParameter;
import cloudgene.mapred.wdl.WdlReader;

public class GetApp extends ServerResource {

	@Get
	public Representation get() {

		UserSessions sessions = UserSessions.getInstance();
		User user = sessions.getUserByRequest(getRequest());

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");

		}

		Settings settings = Settings.getInstance();

		if (settings.isMaintenance() && !user.isAdmin()) {

			setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
			return new StringRepresentation(
					"This functionality is currently under maintenance.");

		}

		String filename = settings.getApp(user);

		WdlApp app;
		try {
			app = WdlReader.loadAppFromFile(filename);
			WdlHeader meta = (WdlHeader) app;

			JsonConfig config = new JsonConfig();
			config.setExcludes(new String[] { "mapred", "installed", "cluster" });
			JSONObject jsonObject = JSONObject.fromObject(meta, config);
			
			List<WdlParameter> params = app.getMapred().getInputs();
			JSONArray jsonArray = JSONArray.fromObject(params);
			jsonObject.put("params", jsonArray);
			jsonObject.put("submitButton", settings.getTemplate(Template.SUBMIT_BUTTON_TEXT));
			

			return new StringRepresentation(jsonObject.toString());
		} catch (IOException e) {
			e.printStackTrace();
			return new StringRepresentation("Error");
		}

	}

}
