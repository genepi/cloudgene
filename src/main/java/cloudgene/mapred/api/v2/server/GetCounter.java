package cloudgene.mapred.api.v2.server;

import java.util.Map;

import cloudgene.mapred.Application;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.jobs.AbstractJob;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import net.sf.json.JSONObject;

@Controller
public class GetCounter {

	@Inject
	protected Application application;
	
	@Get("/api/v2/server/counters")
	@Secured(SecurityRule.IS_ANONYMOUS) 
	public String get() {

		JSONObject jsonCounters = new JSONObject();
		
		// complete
		Map<String, Long> counters = application.getWorkflowEngine().getCounters(
				AbstractJob.STATE_SUCCESS);	
		JSONObject jsonComplete = new JSONObject();	
		for (String key : counters.keySet()) {
			jsonComplete.put(key, counters.get(key));
		}
		jsonCounters.put("complete", jsonComplete);

		// running
		counters = application.getWorkflowEngine().getCounters(AbstractJob.STATE_RUNNING);
		JSONObject jsonRunning = new JSONObject();	
		for (String key : counters.keySet()) {
			jsonRunning.put(key, counters.get(key));
		}
		jsonCounters.put("running", jsonRunning);

		// waiting
		counters = application.getWorkflowEngine().getCounters(AbstractJob.STATE_WAITING);
		JSONObject jsonWaiting= new JSONObject();	
		for (String key : counters.keySet()) {
			jsonWaiting.put(key, counters.get(key));
		}
		jsonCounters.put("waiting", jsonWaiting);

		UserDao dao = new UserDao(application.getDatabase());		
		jsonCounters.put("users", dao.findAll().size());
		
		
		return jsonCounters.toString();

	}

}
