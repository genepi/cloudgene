package cloudgene.mapred.resources.apps;

import genepi.io.FileUtil;

import java.io.IOException;
import java.util.List;

import net.sf.json.JSONArray;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlParameter;
import cloudgene.mapred.wdl.WdlReader;

public class GetAppParams extends BaseResource {

	@Post
	public Representation post(Representation entity) {

		User user = getUser(getRequest());

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");

		}

		Form form = new Form(entity);

		WdlApp app = WdlReader.loadApp(FileUtil.path(getSettings()
				.getAppsPath(), form.getFirstValue("tool")));

		List<WdlParameter> params = app.getMapred().getInputs();

		JSONArray jsonArray = JSONArray.fromObject(params);

		return new StringRepresentation(jsonArray.toString());

	}

	@Get
	public Representation get(Representation entity) {

		User user = getUser(getRequest());

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");

		}
		String filename = getSettings().getApp(user);

		WdlApp app;
		try {
			app = WdlReader.loadAppFromFile(filename);
		} catch (IOException e) {
			e.printStackTrace();
			return new StringRepresentation("Error");
		}

		List<WdlParameter> params = app.getMapred().getInputs();

		JSONArray jsonArray = JSONArray.fromObject(params);

		return new StringRepresentation(jsonArray.toString());

	}

}
