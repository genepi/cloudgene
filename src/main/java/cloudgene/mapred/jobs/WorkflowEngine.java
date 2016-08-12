package cloudgene.mapred.jobs;

import genepi.db.Database;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.CounterDao;
import cloudgene.mapred.database.DownloadDao;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.database.MessageDao;
import cloudgene.mapred.database.ParameterDao;
import cloudgene.mapred.database.StepDao;
import cloudgene.mapred.jobs.queue.PriorityRunnable;
import cloudgene.mapred.jobs.queue.Queue;

public class WorkflowEngine implements Runnable {

	private Thread threadLongTimeQueue;

	private Thread threadShortTimeQueue;

	private Queue longTimeQueue;

	private Queue shortTimeQueue;

	private JobDao dao;

	private CounterDao counterDao;

	private boolean running = false;

	private Database database;

	private Map<String, Long> counters;
	
	private AtomicLong priorityCounter = new AtomicLong();

	private static final Log log = LogFactory.getLog(WorkflowEngine.class);

	public WorkflowEngine(Database mdatabase, int ltqThreads, int stqThreads) {
		this.database = mdatabase;

		log.info("Init Counters....");

		counterDao = new CounterDao(database);
		counters = counterDao.getAll();

		dao = new JobDao(database);

		List<AbstractJob> deadJobs = dao
				.findAllByState(AbstractJob.STATE_WAITING);
		deadJobs.addAll(dao.findAllByState(AbstractJob.STATE_RUNNING));
		deadJobs.addAll(dao.findAllByState(AbstractJob.STATE_EXPORTING));

		for (AbstractJob job : deadJobs) {
			log.info("lost control over job " + job.getId() + " -> Dead");
			job.setState(AbstractJob.STATE_DEAD);
			dao.update(job);
		}

		shortTimeQueue = new Queue("ShortTimeQueue", stqThreads, false, false) {

			@Override
			public PriorityRunnable createRunnable(AbstractJob job) {
				return new SetupThread(job);
			}

			@Override
			public void onComplete(AbstractJob job) {

				if (job.isSetupComplete()) {

					job.setState(AbstractJob.STATE_WAITING);
					dao.update(job);
					longTimeQueue.submit(job);

				} else {

					log.info("Setup failed for Job " + job.getId()
							+ ". Not added to Long Time Queue.");

					dao.update(job);

					DownloadDao downloadDao = new DownloadDao(database);

					for (CloudgeneParameter parameter : job.getOutputParams()) {

						if (parameter.isDownload()) {

							if (((CloudgeneParameter) parameter).getFiles() != null) {

								for (Download download : parameter.getFiles()) {
									download.setParameterId(parameter.getId());
									download.setParameter(parameter);
									downloadDao.insert(download);
								}

							}

						}

					}

					if (job.getSteps() != null) {
						StepDao dao2 = new StepDao(database);
						for (CloudgeneStep step : job.getSteps()) {
							dao2.insert(step);

							MessageDao messageDao = new MessageDao(database);
							if (step.getLogMessages() != null) {
								for (Message logMessage : step.getLogMessages()) {
									messageDao.insert(logMessage);
								}
							}

						}
					}

					// write all submitted counters into database
					for (String name : job.getContext().getSubmittedCounters()
							.keySet()) {
						Integer value = job.getContext().getSubmittedCounters()
								.get(name);

						if (value != null) {

							Long counterValue = counters.get(name);
							if (counterValue == null) {
								counterValue = 0L + value;
							} else {
								counterValue = counterValue + value;
							}
							counters.put(name, counterValue);

							counterDao.insert(name, value, job);

						}
					}

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

				dao.update(job);
				DownloadDao downloadDao = new DownloadDao(database);

				for (CloudgeneParameter parameter : job.getOutputParams()) {

					if (parameter.isDownload()) {

						if (((CloudgeneParameter) parameter).getFiles() != null) {

							for (Download download : parameter.getFiles()) {
								download.setParameterId(parameter.getId());
								download.setParameter(parameter);
								downloadDao.insert(download);
							}

						}

					}

				}

				if (job.getSteps() != null) {
					StepDao dao2 = new StepDao(database);
					for (CloudgeneStep step : job.getSteps()) {
						dao2.insert(step);

						MessageDao messageDao = new MessageDao(database);
						if (step.getLogMessages() != null) {
							for (Message logMessage : step.getLogMessages()) {
								messageDao.insert(logMessage);
							}
						}

					}
				}

				// write all submitted counters into database
				for (String name : job.getContext().getSubmittedCounters()
						.keySet()) {
					Integer value = job.getContext().getSubmittedCounters()
							.get(name);

					if (value != null) {

						Long counterValue = counters.get(name);
						if (counterValue == null) {
							counterValue = 0L + value;
						} else {
							counterValue = counterValue + value;
						}
						counters.put(name, counterValue);

						counterDao.insert(name, value, job);

					}
				}

			}

		};

	}
	public void submit(AbstractJob job) {
		submit(job, priorityCounter.incrementAndGet());
	}

	public void submit(AbstractJob job, long priority) {

		job.setPriority(priority);
		
		dao.insert(job);

		ParameterDao dao = new ParameterDao(database);

		for (CloudgeneParameter parameter : job.getInputParams()) {
			parameter.setJobId(job.getId());
			dao.insert(parameter);
		}

		for (CloudgeneParameter parameter : job.getOutputParams()) {
			parameter.setJobId(job.getId());
			dao.insert(parameter);
		}

		boolean okey = job.afterSubmission();
		if (okey){
			shortTimeQueue.submit(job);
		}else{
			this.dao.update(job);
		}
	}

	public void restart(AbstractJob job) {

		job.setState(AbstractJob.STATE_WAITING);
		dao.update(job);

		boolean okey = job.afterSubmission();
		if (okey){
			shortTimeQueue.submit(job);
		}else{
			dao.update(job);
		}

	}
	
	public void pushToFront(AbstractJob job){
		if (longTimeQueue.isInQueue(job)) {
			longTimeQueue.pushToFront(job);
		}
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
		return running && shortTimeQueue.isRunning()
				&& longTimeQueue.isRunning();
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

		if (state == AbstractJob.STATE_SUCCESS) {

			return counters;

		} else {

			Map<String, Long> result = new HashMap<String, Long>();
			List<AbstractJob> jobs = longTimeQueue.getAllJobs();
			for (AbstractJob job : jobs) {
				if (job.getState() == state) {
					Map<String, Integer> counters = job.getContext()
							.getCounters();
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
			log.info("Input Validation for job " + job.getId()
					+ " finished. Result: " + result);

		}

	}

	public boolean isInQueue(AbstractJob job) {
		if (!shortTimeQueue.isInQueue(job)) {
			return longTimeQueue.isInQueue(job);
		} else {
			return true;
		}
	}
}