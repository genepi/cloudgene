package cloudgene.mapred.api.v2.admin;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.CounterHistoryDao;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.auth.AuthenticationService;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import jakarta.inject.Inject;
import net.sf.json.JSONArray;

@Controller
public class GetStatistics {

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	public String[] counters = new String[] { "runningJobs", "waitingJobs", "completeJobs", "users" };

	@Get("/api/v2/admin/server/statistics")
	@Secured(User.ROLE_ADMIN)
	public String getStatistics(@Nullable @QueryValue("days") Integer days) {

		if (days == null) {
			days = 1;
		}

		CounterHistoryDao dao = new CounterHistoryDao(application.getDatabase());

		List<Map<String, String>> stats = dao.getAllBeetween(
				System.currentTimeMillis() - (1000L * 60L * 60L * 24L * days), System.currentTimeMillis());

		// minimize points
		List<Map<String, String>> toRemove = new Vector<Map<String, String>>();
		for (int i = 1; i < stats.size() - 1; i++) {
			Map<String, String> prev = stats.get(i - 1);
			Map<String, String> current = stats.get(i);
			Map<String, String> next = stats.get(i + 1);

			if (equals(prev, current, counters) && equals(current, next, counters)) {
				toRemove.add(current);
			}

		}
		stats.removeAll(toRemove);
		JSONArray jsonArray = JSONArray.fromObject(stats);

		return jsonArray.toString();

	}

	private boolean equals(Map<String, String> a, Map<String, String> b, String[] counters) {

		for (String key : counters) {

			if (a.get(key) == null) {
				return false;
			}

			if (b.get(key) == null) {
				return false;
			}

			if (!a.get(key).equals(b.get(key))) {
				return false;
			}
		}
		return true;
	}

}
