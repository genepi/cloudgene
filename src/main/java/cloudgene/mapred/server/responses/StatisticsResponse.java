package cloudgene.mapred.server.responses;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class StatisticsResponse extends HashMap<String, String> {

	private static final long serialVersionUID = 1L;

	public static String[] counters = new String[] { "runningJobs", "waitingJobs", "completeJobs", "users" };

	public static List<Map<String, String>> build(List<Map<String, String>> stats) {

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

		return stats;

	}

	private static boolean equals(Map<String, String> a, Map<String, String> b, String[] counters) {

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
