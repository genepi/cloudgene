package cloudgene.mapred.resources.apps;

import java.io.IOException;

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
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlHeader;
import cloudgene.mapred.wdl.WdlReader;

public class GetApp extends ServerResource {

	@Get
	public Representation get() {

		UserSessions sessions = UserSessions.getInstance();
		User user = sessions.getUserByRequest(getRequest());

		if (user != null) {

			String filename = Settings.getInstance().getApp();

			WdlApp app;
			try {
				app = WdlReader.loadAppFromFile(filename);
				WdlHeader meta = (WdlHeader) app;

				JsonConfig config = new JsonConfig();
				config.setExcludes(new String[] { "mapred", "installed",
						"cluster" });
				JSONObject jsonObject = JSONObject.fromObject(meta, config);

				return new StringRepresentation(jsonObject.toString());
			} catch (IOException e) {
				e.printStackTrace();
				return new StringRepresentation("Error");
			}

		} else {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED );
			return new StringRepresentation("The request requires user authentication.");

		}

	}

}
