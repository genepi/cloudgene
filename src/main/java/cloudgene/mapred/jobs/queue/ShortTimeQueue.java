package cloudgene.mapred.jobs.queue;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;

public class ShortTimeQueue implements Runnable {

	private BlockingQueue<Runnable> queueThreadPool;

	private List<AbstractJob> queue;

	private HashMap<AbstractJob, Future<?>> futures;

	private JobDao dao;

	private int THREADS = 5;

	private ThreadPoolExecutor threadPool;

	private static final Log log = LogFactory.getLog(ShortTimeQueue.class);

	public ShortTimeQueue() {
		futures = new HashMap<AbstractJob, Future<?>>();
		queue = new Vector<AbstractJob>();

		queueThreadPool = new ArrayBlockingQueue<Runnable>(100);

		threadPool = new ThreadPoolExecutor(THREADS, THREADS, 10,
				TimeUnit.SECONDS, queueThreadPool);

		dao = new JobDao();

	}

	public void submit(AbstractJob job) {

		Future<?> future = threadPool.submit(new SetupThread(job));
		futures.put(job, future);
		queue.add(job);
		log.info("Short Time Queue: Submit job...");

	}

	public void cancel(AbstractJob job) {

		if (job.getState() == AbstractJob.RUNNING) {

			log.info("Short Time Queue: Cancel Job ...");

			job.cancel();
			job.kill();

			Future<?> future = futures.get(job);
			future.cancel(true);

			futures.remove(job);
			queue.remove(job);

			dao.insert(job);

		}

		if (job.getState() == AbstractJob.WAITING) {

			log.info("Short Time Queue: Cancel Job...");

			job.cancel();

			queue.remove(job);
			futures.remove(job);
			dao.insert(job);
		}

	}

	@Override
	public void run() {
		while (true) {
			List<AbstractJob> complete = new Vector<AbstractJob>();
			for (AbstractJob job : futures.keySet()) {
				Future<?> future = futures.get(job);
				if (future.isDone() || future.isCancelled()) {
					log.info("Short Time Queue: Job " + job.getId()
							+ ": finished");
					queue.remove(job);
					complete.add(job);
				}
			}

			for (AbstractJob job : complete) {
				futures.remove(job);
				onComplete(job);
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public List<AbstractJob> getJobsByUser(User user) {

		List<AbstractJob> result = new Vector<AbstractJob>();

		for (AbstractJob job : queue) {

			if (job.getUser().getId() == user.getId()) {
				result.add(job);
			}

		}

		return result;
	}

	public List<AbstractJob> getAllJobs() {

		List<AbstractJob> result = new Vector<AbstractJob>();

		for (AbstractJob job : queue) {

			result.add(job);

		}

		return result;
	}

	public AbstractJob getJobById(String id) {

		for (AbstractJob job : queue) {

			if (job.getId().equals(id)) {
				return job;
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
