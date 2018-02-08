package cloudgene.mapred.api.v2.admin;

import java.util.List;
import java.util.Map;
import java.util.Vector;

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

	public String[] counters = new String[] { "runningJobs", "waitingJobs",
			"completeJobs", "users" };

	@Get
	public Representation getStatistics() {

		User user = getAuthUser();
		long days = 1;
		if (getQueryValue("days") != null) {
			days = Long.parseLong(getQueryValue("days"));
		}

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

		List<Map<String, String>> stats = dao.getAllBeetween(
				System.currentTimeMillis() - (1000L * 60L * 60L * 24L * days),
				System.currentTimeMillis());

		// minimize points
		List<Map<String, String>> toRemove = new Vector<Map<String, String>>();
		for (int i = 1; i < stats.size() - 1; i++) {
			Map<String, String> prev = stats.get(i - 1);
			Map<String, String> current = stats.get(i);
			Map<String, String> next = stats.get(i + 1);

			if (equals(prev, current, counters)
					&& equals(current, next, counters)) {
				toRemove.add(current);
			}

		}
		stats.removeAll(toRemove);
		JSONArray jsonArray = JSONArray.fromObject(stats);

		return new StringRepresentation(jsonArray.toString());

	}

	private boolean equals(Map<String, String> a, Map<String, String> b,
			String[] counters) {
		
		
		for (String key : counters) {

			if (a.get(key) == null){
				return false;
			}
			
			if (b.get(key) == null){
				return false;
			}
			
			
			if (!a.get(key).equals(b.get(key))) {
				return false;
			}
		}
		return true;
	}

}
