package cloudgene.mapred.jobs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.queue.LongTimeQueue;
import cloudgene.mapred.jobs.queue.ShortTimeQueue;

public class WorkflowEngine implements Runnable {

	static WorkflowEngine instance = null;

	private LongTimeQueue longTimeQueue;

	private ShortTimeQueue shortTimeQueue;

	private JobDao dao;

	private List<AbstractJob> longTimeJobs = new Vector<AbstractJob>();
	private List<AbstractJob> shortTimeJobs = new Vector<AbstractJob>();

	private static final Log log = LogFactory.getLog(WorkflowEngine.class);

	public static WorkflowEngine getInstance() {
		if (instance == null) {
			instance = new WorkflowEngine();
		}
		return instance;
	}

	private WorkflowEngine() {

		dao = new JobDao();
		longTimeQueue = new LongTimeQueue();
		shortTimeQueue = new ShortTimeQueue() {

			public void onComplete(AbstractJob job) {

				shortTimeJobs.remove(job);

				if (job.isSetupComplete()) {

					longTimeJobs.add(job);
					job.setState(AbstractJob.WAITING);
					longTimeQueue.submit(job);

				} else {

					job.setState(AbstractJob.ERROR);
					// job.writeLog("Job execution failed: Setup Step failed.");
					job.onFailure();
					job.setStartTime(System.currentTimeMillis());
					job.setEndTime(System.currentTimeMillis());
					dao.insert(job);

					log.info("Setup failed for Job " + job.getId()
							+ ". Not added to Long Time Queue.");
				}

			}

		};

	}

	public void submit(AbstractJob job) {

		job.afterSubmission();

		shortTimeJobs.add(job);
		shortTimeQueue.submit(job);

	}

	public void cancel(AbstractJob job) {

		if (shortTimeJobs.contains(job)) {
			shortTimeJobs.remove(job);
			shortTimeQueue.cancel(job);
		}

		if (longTimeJobs.contains(job)) {
			longTimeJobs.remove(job);
			longTimeQueue.cancel(job);
		}

	}

	@Override
	public void run() {

		new Thread(longTimeQueue).start();
		new Thread(shortTimeQueue).start();

	}

	public List<AbstractJob> getAllJobsInLongTimeQueue() {
		return longTimeQueue.getAllJobs();
	}

	public AbstractJob getJobById(String id) {
		AbstractJob job = longTimeQueue.getJobById(id);
		if (job == null) {
			job = shortTimeQueue.getJobById(id);
		}
		return job;
	}
	
	public Map<String, Long> getCounters(int state){
		Map<String, Long> result = new HashMap<String, Long>();
		List<AbstractJob> jobs = longTimeQueue.getAllJobs();
		for (AbstractJob job: jobs){
			if (job.getState() == state){
				Map<String, Integer> counters = job.getContext().getCounters();
				for (String name: counters.keySet()){
					Integer value = counters.get(name);
					Long oldvalue = result.get(name);
					if (oldvalue == null) {
						oldvalue = new Long(0);
					}
					result.put(name, oldvalue + value);
				}
			}
		}
		return result;
	}

	public List<AbstractJob> getJobsByUser(User user) {

		List<AbstractJob> jobs = shortTimeQueue.getJobsByUser(user);
		jobs.addAll(longTimeQueue.getJobsByUser(user));
		return jobs;
	}

	public List<AbstractJob> getAllJobsInShortTimeQueue() {
		return shortTimeQueue.getAllJobs();
	}

	public int getPositionInQueue(AbstractJob job) {
		return longTimeQueue.getPositionInQueue(job);
	}

}
