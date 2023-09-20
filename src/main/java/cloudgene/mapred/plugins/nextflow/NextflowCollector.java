package cloudgene.mapred.plugins.nextflow;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.util.Settings;

public class NextflowCollector {

	private static final String COLLECTOR_ENDPOINT = "/api/v2/collect/";

	private static NextflowCollector instance;

	private Map<String, List<NextflowProcess>> data;

	private Map<String, CloudgeneContext> contexts;

	private static final Logger log = LoggerFactory.getLogger(NextflowCollector.class);

	public static NextflowCollector getInstance() {
		if (instance == null) {
			instance = new NextflowCollector();
		}

		return instance;
	}

	private NextflowCollector() {
		contexts = new HashMap<String, CloudgeneContext>();
		data = new HashMap<String, List<NextflowProcess>>();
	}

	public String addContext(CloudgeneContext context) {
		contexts.put(context.getPublicJobId(), context);
		Settings settings = context.getSettings();
		log.info("[Job {}] Register collector for public job id '{}'", context.getJobId(), context.getPublicJobId());
		return settings.getServerUrl() + settings.getUrlPrefix() + COLLECTOR_ENDPOINT + context.getPublicJobId();
	}

	public void addEvent(String job, Map<String, Object> event) throws IOException {

		CloudgeneContext context = contexts.get(job);

		if (context == null) {
			log.info("Warning! No context found for public job id '{}'", job);
			return;
		}

		List<NextflowProcess> processes = data.get(job);
		if (processes == null) {
			processes = new Vector<NextflowProcess>();
			data.put(job, processes);
		}

		if (!event.containsKey("trace")) {
			return;
		}

		Map<String, Object> trace = (Map<String, Object>) event.get("trace");
		if (!trace.containsKey("process")) {
			return;
		}

		String processName = trace.get("process").toString();
		for (NextflowProcess process : processes) {
			if (process.getName().equals(processName)) {
				process.addTrace(trace);
				return;
			}
		}
		NextflowProcess process = new NextflowProcess(context, trace);
		processes.add(process);
	}

	public List<NextflowProcess> getProcesses(CloudgeneContext context) {
		List<NextflowProcess> processes = data.get(context.getPublicJobId());
		if (processes == null) {
			return new Vector<NextflowProcess>();
		}
		return processes;

	}

	public void cleanProcesses(CloudgeneContext context) {
		data.remove(context.getPublicJobId());
		contexts.remove(context.getPublicJobId());
		log.info("[Job {}] Removed collector for public job id '{}'", context.getJobId(), context.getPublicJobId());
	}

}
