package cloudgene.mapred.resources.apps;

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
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlParameter;
import cloudgene.mapred.wdl.WdlReader;

public class GetAppParams extends ServerResource {

	@Post
	public Representation post(Representation entity) {
		UserSessions sessions = UserSessions.getInstance();
		User user = sessions.getUserByRequest(getRequest());

		Form form = new Form(entity);

		if (user != null) {

			WdlApp app = WdlReader.loadApp(form.getFirstValue("tool"));

			List<WdlParameter> params = app.getMapred().getInputs();

			JSONArray jsonArray = JSONArray.fromObject(params);

			return new StringRepresentation(jsonArray.toString());

		} else {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED );
			return new StringRepresentation("The request requires user authentication.");

		}
	}

	@Get
	public Representation get(Representation entity) {
		UserSessions sessions = UserSessions.getInstance();
		User user = sessions.getUserByRequest(getRequest());

		if (user != null) {

			String filename = Settings.getInstance().getApp(user);

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

		} else {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED );
			return new StringRepresentation("The request requires user authentication.");

		}
	}

}
