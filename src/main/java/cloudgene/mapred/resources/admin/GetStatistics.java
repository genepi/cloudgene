package cloudgene.mapred.resources.admin;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.CounterHistoryDao;
import cloudgene.mapred.util.BaseResource;

public class GetStatistics extends BaseResource {

	/**
	 * Resource to get statistics
	 */

	@Get
	public Representation getStatistics() {

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

		CounterHistoryDao dao = new CounterHistoryDao(getDatabase());

		List<Map<String, String>> stats = dao.getAll(2*720);
		JSONArray jsonArray = JSONArray.fromObject(stats);

		return new StringRepresentation(jsonArray.toString());

	}

}
