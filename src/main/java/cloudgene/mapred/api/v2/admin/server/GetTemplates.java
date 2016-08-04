package cloudgene.mapred.api.v2.admin.server;

import java.util.List;

import net.sf.json.JSONArray;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.TemplateDao;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.Template;

public class GetTemplates extends BaseResource {

	@Get
	public Representation get() {

		User user = getAuthUser();

		if (user == null) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");
		}

		if (!user.isAdmin()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires administration rights.");
		}

		TemplateDao dao = new TemplateDao(getDatabase());
		List<Template> templates = dao.findAll();

		JSONArray jsonArray = JSONArray.fromObject(templates);

		return new StringRepresentation(jsonArray.toString());

	}

}
