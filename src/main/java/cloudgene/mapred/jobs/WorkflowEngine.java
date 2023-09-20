package cloudgene.mapred.jobs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.queue.PriorityRunnable;
import cloudgene.mapred.jobs.queue.Queue;

public class WorkflowEngine implements Runnable {

	private Thread threadLongTimeQueue;

	private Queue longTimeQueue;

	private boolean running = false;

	private AtomicLong priorityCounter = new AtomicLong();

	private static final Logger log = LoggerFactory.getLogger(WorkflowEngine.class);

	public WorkflowEngine(int ltqThreads, int stqThreads) {

		longTimeQueue = new Queue("LongTimeQueue", ltqThreads, true, true) {

			@Override
			public PriorityRunnable createRunnable(AbstractJob job) {
				return job;
			}

			@Override
			public void onComplete(AbstractJob job) {
				job.setEndTime(System.currentTimeMillis());
				jobCompleted(job);
			}

		};

	}

	public void submit(AbstractJob job) {
		submit(job, priorityCounter.incrementAndGet());
	}

	public void submit(AbstractJob job, long priority) {

		job.setPriority(priority);
		job.setSubmittedOn(System.currentTimeMillis());
		jobSubmitted(job);

		boolean okey = job.afterSubmission();
		if (okey) {
			longTimeQueue.submit(job);
		} else {
			job.setEndTime(System.currentTimeMillis());
			statusUpdated(job);
		}
	}

	public void restart(AbstractJob job) {
		restart(job, priorityCounter.incrementAndGet());
	}

	public void restart(AbstractJob job, long priority) {

		job.setPriority(priority);
		job.setSubmittedOn(System.currentTimeMillis());
		job.setStartTime(0);
		job.setEndTime(0);
		job.setState(AbstractJob.STATE_WAITING);
		statusUpdated(job);

		boolean okey = job.afterSubmission();
		if (okey) {
			longTimeQueue.submit(job);
		} else {
			job.setEndTime(System.currentTimeMillis());
			statusUpdated(job);
		}

	}

	public void updatePriority(AbstractJob job, long priority) {
		longTimeQueue.updatePriority(job, priority);
	}

	public void cancel(AbstractJob job) {
		if (longTimeQueue.isInQueue(job)) {
			longTimeQueue.cancel(job);
		}
	}

	@Override
	public void run() {
		threadLongTimeQueue = new Thread(longTimeQueue);
		threadLongTimeQueue.start();
		running = true;

	}

	public void stop() {
		threadLongTimeQueue.stop();
	}

	public void block() {
		longTimeQueue.pause();
		running = false;
	}

	public void resume() {
		longTimeQueue.resume();
		running = true;
	}

	public boolean isRunning() {
		return running && longTimeQueue.isRunning();
	}

	public int getActiveCount() {
		return longTimeQueue.getActiveCount();
	}

	public AbstractJob getJobById(String id) {
		return longTimeQueue.getJobById(id);
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

		List<AbstractJob> jobs = longTimeQueue.getJobsByUser(user);

		for (AbstractJob job : jobs) {

			if (job instanceof CloudgeneJob) {

				((CloudgeneJob) job).updateProgress();

			}

		}

		return jobs;
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

	public boolean isInQueue(AbstractJob job) {
		return longTimeQueue.isInQueue(job);
	}

	protected void statusUpdated(AbstractJob job) {

	}

	protected void jobCompleted(AbstractJob job) {

	}

	protected void jobSubmitted(AbstractJob job) {

	}

}