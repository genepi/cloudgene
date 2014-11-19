package cloudgene.mapred.resources.apps;

import genepi.io.FileUtil;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JsonConfig;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.Category;
import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.wdl.WdlReader;

public class GetApps extends BaseResource {

	@Get
	public Representation get() {

		User user = getUser(getRequest());

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");

		}

		List<Category> apps = WdlReader.loadApps(getSettings().getAppsPath());
		JsonConfig config = new JsonConfig();
		config.setExcludes(new String[] { "mapred", "installed", "cluster" });
		JSONArray jsonArray = JSONArray.fromObject(apps, config);

		return new StringRepresentation(jsonArray.toString());

	}

}
