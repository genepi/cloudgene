package cloudgene.mapred.api.v2.server;

import java.util.Map;

import cloudgene.mapred.database.JobDao;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.BaseResource;
import net.sf.json.JSONObject;

public class GetCounter extends BaseResource {

	@Get
	public Representation get() {

		JSONObject jsonCounters = new JSONObject();

		// complete
		Map<String, Long> counters = getWorkflowEngine().getCounters(
				AbstractJob.STATE_SUCCESS);
		JSONObject jsonComplete = new JSONObject();
		for (String key : counters.keySet()) {
			jsonComplete.put(key, counters.get(key));
		}
		jsonCounters.put("complete", jsonComplete);

		// running
		counters = getWorkflowEngine().getCounters(AbstractJob.STATE_RUNNING);
		JSONObject jsonRunning = new JSONObject();
		for (String key : counters.keySet()) {
			jsonRunning.put(key, counters.get(key));
		}
		jsonCounters.put("running", jsonRunning);

		// waiting
		counters = getWorkflowEngine().getCounters(AbstractJob.STATE_WAITING);
		JSONObject jsonWaiting= new JSONObject();
		for (String key : counters.keySet()) {
			jsonWaiting.put(key, counters.get(key));
		}
		jsonCounters.put("waiting", jsonWaiting);

		UserDao dao = new UserDao(getDatabase());
		jsonCounters.put("users", dao.countAll());

		JSONObject queue = new JSONObject();
		queue.put("size", getWorkflowEngine().getSize());
		jsonCounters.put("queue", queue);

		return new StringRepresentation(jsonCounters.toString());

	}

}
