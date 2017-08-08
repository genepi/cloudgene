package cloudgene.mapred.api.v2.server;

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
import net.sf.json.JSONArray;

public class Apps extends BaseResource {

	@Post
	public Representation install(Representation entity) {

		User user = getAuthUser();

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires user authentication.");

		}

		if (!user.isAdmin()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires administration rights.");
		}

		Form form = new Form(entity);
		String url = form.getFirstValue("url");

		if (url == null) {
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return new StringRepresentation("No url or file location set.");
		}

		try {

			Application application = null;

			if (url.startsWith("http://")) {
				application = getSettings().installApplicationFromUrl(url);
			} else {
				if (url.endsWith(".zip")) {
					application = getSettings().installApplicationFromZipFile(url);
				} else {
					application = getSettings().installApplicationFromDirectory(url);
				}
			}

			getSettings().save();
			if (application != null) {
				return new JsonRepresentation(application);
			} else {
				setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				return new StringRepresentation("Application not installed: No workflow file found.");
			}

		} catch (Exception e) {
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return new StringRepresentation("Application not installed: " + e.getMessage());
		}

	}

	@Get
	public Representation get() {

		User user = getAuthUser();

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires user authentication.");

		}

		List<Application> apps = getSettings().getApps();
		JSONArray jsonArray = JSONArray.fromObject(apps);

		return new StringRepresentation(jsonArray.toString());

	}

}
