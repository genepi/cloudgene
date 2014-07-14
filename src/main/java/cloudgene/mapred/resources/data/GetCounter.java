package cloudgene.mapred.resources.data;

import java.util.Map;

import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.database.CounterDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.WorkflowEngine;

public class GetCounter extends ServerResource {

	@Get
	public Representation get() {

		CounterDao dao = new CounterDao();
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
		counters = WorkflowEngine.getInstance()
				.getCounters(AbstractJob.RUNNING);
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
		counters = WorkflowEngine.getInstance()
				.getCounters(AbstractJob.WAITING);
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
