package cloudgene.mapred.jobs.queue;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;

public class ShortTimeQueue implements Runnable {

	private List<AbstractJob> queue;

	private HashMap<AbstractJob, Future<?>> futures;

	private JobDao dao;

	private int THREADS = 5;

	private Scheduler scheduler;

	private static final Log log = LogFactory.getLog(ShortTimeQueue.class);

	public ShortTimeQueue() {
		futures = new HashMap<AbstractJob, Future<?>>();
		queue = new Vector<AbstractJob>();
		scheduler = new Scheduler(THREADS);
		dao = new JobDao();

	}

	public void submit(AbstractJob job) {

		synchronized (futures) {

			synchronized (queue) {

				Future<?> future = scheduler.submit(new SetupThread(job));
				futures.put(job, future);
				queue.add(job);
				log.info("Short Time Queue: Submit job...");

			}

		}

	}

	public void cancel(AbstractJob job) {

		if (job.getState() == AbstractJob.STATE_RUNNING) {

			log.info("Short Time Queue: Cancel Job ...");

			job.cancel();
			job.kill();

		}

		if (job.getState() == AbstractJob.STATE_WAITING) {

			synchronized (futures) {

				synchronized (queue) {
					scheduler.kill(job);
					job.setStartTime(System.currentTimeMillis());
					job.cancel();
					queue.remove(job);
					futures.remove(job);
					dao.insert(job);
					log.info("Short Time Queue: Cancel Job...");
				}

			}
		}

	}

	@Override
	public void run() {

		while (true) {
			try {

				synchronized (futures) {

					List<AbstractJob> complete = new Vector<AbstractJob>();

					synchronized (queue) {

						for (AbstractJob job : futures.keySet()) {
							Future<?> future = futures.get(job);
							if (future.isDone() || future.isCancelled()) {
								log.info("Short Time Queue: Job " + job.getId()
										+ ": finished");
								queue.remove(job);
								complete.add(job);
							}

						}

					}

					for (AbstractJob job : complete) {
						futures.remove(job);
						onComplete(job);
					}

				}

				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			} catch (Exception e) {

				log.warn("Short Time Queue: Concurrency Exception!! ");

			}

		}
	}

	public void pause() {
		log.info("Short Time Queue: Pause...");
		scheduler.pause();
	}

	public void resume() {
		log.info("Short Time Queue: Resume...");
		scheduler.resume();
	}

	public boolean isRunning() {
		return scheduler.isRunning();
	}

	public int getActiveCount() {
		return scheduler.getActiveCount();
	}

	public List<AbstractJob> getJobsByUser(User user) {

		List<AbstractJob> result = new Vector<AbstractJob>();

		synchronized (queue) {

			for (AbstractJob job : queue) {

				if (job.getUser().getId() == user.getId()) {
					result.add(job);
				}

			}

		}

		return result;
	}

	public List<AbstractJob> getAllJobs() {

		List<AbstractJob> result = new Vector<AbstractJob>();

		synchronized (queue) {

			for (AbstractJob job : queue) {

				result.add(job);

			}

		}

		return result;
	}

	public AbstractJob getJobById(String id) {

		synchronized (queue) {

			for (AbstractJob job : queue) {

				if (job.getId().equals(id)) {
					return job;
				}

			}

		}

		return null;
	}

	public void onComplete(AbstractJob job) {

	}

	class SetupThread implements Runnable {

		private AbstractJob job;

		public SetupThread(AbstractJob job) {
			this.job = job;
		}

		@Override
		public void run() {
			log.info("Short Time Queue: Running Job " + job.getId());
			boolean result = job.executeSetup();
			job.setSetupComplete(result);
			log.info("Short Time Queue: Job " + job.getId()
					+ " finished. Result: " + result);
		}

	}

}
