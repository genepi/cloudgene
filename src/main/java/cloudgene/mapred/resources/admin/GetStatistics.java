package cloudgene.mapred.resources.admin;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.database.CounterHistoryDao;

public class GetStatistics extends ServerResource {

	/**
	 * Resource to get job status information
	 */

	@Get
	public Representation getStatistics() {

		UserSessions sessions = UserSessions.getInstance();
		User user = sessions.getUserByRequest(getRequest());

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

		CounterHistoryDao dao = new CounterHistoryDao();

		List<Map<String, String>> stats = dao.getAll();
		JSONArray jsonArray = JSONArray.fromObject(stats);

		return new StringRepresentation(jsonArray.toString());

	}

}
