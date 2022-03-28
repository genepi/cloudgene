package cloudgene.mapred.api.v2.admin;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import cloudgene.mapred.Application;
import cloudgene.mapred.auth.AuthenticationService;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.CounterHistoryDao;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import net.sf.json.JSONArray;

@Controller
public class GetStatistics {

	/**
	 * Resource to get statistics
	 */

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	public String[] counters = new String[] { "runningJobs", "waitingJobs", "completeJobs", "users" };

	@Get("/api/v2/admin/server/statistics")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public String getStatistics(Authentication authentication, @Nullable @QueryValue("days") Integer days) {

		User user = authenticationService.getUserByAuthentication(authentication);

		if (!user.isAdmin()) {
			throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "The request requires administration rights.");
		}

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
