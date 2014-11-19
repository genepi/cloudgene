package cloudgene.mapred.resources.data;

import java.util.Map;

import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.database.CounterDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.util.BaseResource;

public class GetCounter extends BaseResource {

	@Get
	public Representation get() {

		CounterDao dao = new CounterDao(getDatabase());
		Map<String, Long> counters = dao.getAll();

		// complete
		String temp = "{\"complete\": {";
		boolean first = true;
		for (String key : counters.keySet()) {

			if (!first) {
				temp += ",";
			}

			temp += "\"" + key + "\": \"" + counters.get(key) + "\"";
			first = false;
		}
		temp += "},";

		// running
		counters = getWorkflowEngine().getCounters(AbstractJob.STATE_RUNNING);
		temp += "\"running\": {";
		first = true;
		for (String key : counters.keySet()) {

			if (!first) {
				temp += ",";
			}

			temp += "\"" + key + "\": \"" + counters.get(key) + "\"";
			first = false;
		}
		temp += "},";

		// waiting
		counters = getWorkflowEngine().getCounters(AbstractJob.STATE_WAITING);
		temp += "\"waiting\": {";
		first = true;
		for (String key : counters.keySet()) {

			if (!first) {
				temp += ",";
			}

			temp += "\"" + key + "\": \"" + counters.get(key) + "\"";
			first = false;
		}
		temp += "}";

		temp += "}";

		return new StringRepresentation(temp);

	}

}
