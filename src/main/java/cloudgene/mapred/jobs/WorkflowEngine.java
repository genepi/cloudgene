package cloudgene.mapred.jobs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.queue.PriorityRunnable;
import cloudgene.mapred.jobs.queue.Queue;

public class WorkflowEngine implements Runnable {

	private Thread threadLongTimeQueue;

	private Thread threadShortTimeQueue;

	private Queue longTimeQueue;

	private Queue shortTimeQueue;

	private boolean running = false;

	private AtomicLong priorityCounter = new AtomicLong();

	private static final Log log = LogFactory.getLog(WorkflowEngine.class);

	public WorkflowEngine(int ltqThreads, int stqThreads) {

		shortTimeQueue = new Queue("ShortTimeQueue", stqThreads, false, false) {

			@Override
			public PriorityRunnable createRunnable(AbstractJob job) {
				return new SetupThread(job);
			}

			@Override
			public void onComplete(AbstractJob job) {

				if (job.isSetupComplete()) {

					if (job.hasSteps()) {

						job.setState(AbstractJob.STATE_WAITING);
						statusUpdated(job);
						longTimeQueue.submit(job);

					} else {
						job.setState(AbstractJob.STATE_SUCCESS);
						statusUpdated(job);
						jobCompleted(job);

					}

				} else {

					log.info("Setup failed for Job " + job.getId() + ". Not added to Long Time Queue.");
					jobCompleted(job);

				}

			}

		};

		longTimeQueue = new Queue("LongTimeQueue", ltqThreads, true, true) {

			@Override
			public PriorityRunnable createRunnable(AbstractJob job) {
				return job;
			}

			@Override
			public void onComplete(AbstractJob job) {

				jobCompleted(job);

			}

		};

	}

	public void submit(AbstractJob job) {
		submit(job, priorityCounter.incrementAndGet());
	}

	public void submit(AbstractJob job, long priority) {

		job.setPriority(priority);

		jobSubmitted(job);

		boolean okey = job.afterSubmission();
		if (okey) {
			shortTimeQueue.submit(job);
		} else {
			statusUpdated(job);
		}
	}

	public void restart(AbstractJob job) {
		restart(job, priorityCounter.incrementAndGet());
	}

	public void restart(AbstractJob job, long priority) {

		job.setPriority(priority);

		job.setState(AbstractJob.STATE_WAITING);
		statusUpdated(job);

		boolean okey = job.afterSubmission();
		if (okey) {
			shortTimeQueue.submit(job);
		} else {
			statusUpdated(job);
		}

	}

	public void updatePriority(AbstractJob job, long priority) {
		longTimeQueue.updatePriority(job, priority);
	}

	public void cancel(AbstractJob job) {

		if (shortTimeQueue.isInQueue(job)) {
			job.setStartTime(System.currentTimeMillis());
			shortTimeQueue.cancel(job);
		}

		if (longTimeQueue.isInQueue(job)) {
			longTimeQueue.cancel(job);
		}

	}

	@Override
	public void run() {
		threadShortTimeQueue = new Thread(shortTimeQueue);
		threadShortTimeQueue.start();
		threadLongTimeQueue = new Thread(longTimeQueue);
		threadLongTimeQueue.start();
		running = true;

	}

	public void stop() {
		threadShortTimeQueue.stop();
		threadLongTimeQueue.stop();
	}

	public void block() {
		shortTimeQueue.pause();
		longTimeQueue.pause();
		running = false;
	}

	public void resume() {
		shortTimeQueue.resume();
		longTimeQueue.resume();
		running = true;
	}

	public boolean isRunning() {
		return running && shortTimeQueue.isRunning() && longTimeQueue.isRunning();
	}

	public int getActiveCount() {
		return shortTimeQueue.getActiveCount() + longTimeQueue.getActiveCount();
	}

	public AbstractJob getJobById(String id) {
		AbstractJob job = longTimeQueue.getJobById(id);
		if (job == null) {
			job = shortTimeQueue.getJobById(id);
		}
		return job;
	}

	public Map<String, Long> getCounters(int state) {

		Map<String, Long> result = new HashMap<String, Long>();
		List<AbstractJob> jobs = longTimeQueue.getAllJobs();
		for (AbstractJob job : jobs) {
			if (job.getState() == state) {
				Map<String, Integer> counters = job.getContext().getCounters();
				for (String name : counters.keySet()) {
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

		for (AbstractJob job : jobs) {

			if (job instanceof CloudgeneJob) {

				((CloudgeneJob) job).updateProgress();

			}

		}

		return jobs;
	}

	public List<AbstractJob> getAllJobsInShortTimeQueue() {

		List<AbstractJob> jobs = shortTimeQueue.getAllJobs();

		for (AbstractJob job : jobs) {

			if (job instanceof CloudgeneJob) {

				((CloudgeneJob) job).updateProgress();

			}

		}

		return shortTimeQueue.getAllJobs();
	}

	public List<AbstractJob> getAllJobsInLongTimeQueue() {

		List<AbstractJob> jobs = longTimeQueue.getAllJobs();

		for (AbstractJob job : jobs) {

			if (job instanceof CloudgeneJob) {

				((CloudgeneJob) job).updateProgress();

			}

		}

		return jobs;
	}

	class SetupThread extends PriorityRunnable {

		private AbstractJob job;

		public SetupThread(AbstractJob job) {
			this.job = job;
		}

		@Override
		public void run() {
			log.info("Start iput validation for job " + job.getId() + "...");
			boolean result = job.executeSetup();
			job.setSetupComplete(result);
			log.info("Input Validation for job " + job.getId() + " finished. Result: " + result);

		}

	}

	public boolean isInQueue(AbstractJob job) {
		if (!shortTimeQueue.isInQueue(job)) {
			return longTimeQueue.isInQueue(job);
		} else {
			return true;
		}
	}

	protected void statusUpdated(AbstractJob job) {

	}

	protected void jobCompleted(AbstractJob job) {

	}

	protected void jobSubmitted(AbstractJob job) {

	}

}