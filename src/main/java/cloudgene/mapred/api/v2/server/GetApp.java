package cloudgene.mapred.api.v2.server;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.Template;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlHeader;
import cloudgene.mapred.wdl.WdlParameter;
import cloudgene.mapred.wdl.WdlReader;

public class GetApp extends BaseResource {

	@Get
	public Representation post(Representation entity) {

		User user = getAuthUser();

		String tool = getAttribute("tool");

		String filename = getSettings().getApp(user, tool);
		WdlApp app = null;
		WdlHeader meta = null;
		try {
			app = WdlReader.loadAppFromFile(filename);
			meta = (WdlHeader) app;
			meta.setId(tool);

		} catch (Exception e1) {

			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation("Tool '" + tool + "' not found or the request requires user authentication..");

		}

		if (getSettings().isMaintenance() &&( user == null || !user.isAdmin())) {

			setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
			return new StringRepresentation(
					"This functionality is currently under maintenance.");

		}
		
		JsonConfig config = new JsonConfig();
		config.setExcludes(new String[] { "mapred", "installed", "cluster" });
		JSONObject jsonObject = JSONObject.fromObject(meta, config);

		List<WdlParameter> params = app.getMapred().getInputs();
		JSONArray jsonArray = JSONArray.fromObject(params);
		jsonObject.put("params", jsonArray);
		jsonObject.put("submitButton",
				getWebApp().getTemplate(Template.SUBMIT_BUTTON_TEXT));

		return new StringRepresentation(jsonObject.toString());

	}

}
