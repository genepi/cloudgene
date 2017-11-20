package cloudgene.mapred.api.v2.apps;

import java.util.List;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.util.Application;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.wdl.WdlHeader;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class GetUserApps extends BaseResource {

	@Get
	public Representation get() {

		User user = getAuthUser();

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires user authentication.");

		}

		String reload = getQueryValue("reload");
		if (reload != null && reload.equals("true")) {
			getSettings().reloadApplications();
		}

		List<WdlHeader> apps = getSettings().getAppsByUser(user);

		JSONArray jsonArray = new JSONArray();
		for (WdlHeader app : apps) {
			JSONObject object = new JSONObject();
			object.put("id", app.getId());
			object.put("name", app.getName());
			object.put("description", app.getDescription());
			jsonArray.add(object);
		}

		return new StringRepresentation(jsonArray.toString());

	}

}
