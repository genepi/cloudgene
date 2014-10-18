package cloudgene.mapred.jobs.queue;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;

import com.esotericsoftware.yamlbeans.YamlWriter;

public class LongTimeQueue implements Runnable {

	public static String QUEUE_FILENAME = "imputation.queue";

	private List<AbstractJob> queue;

	private HashMap<AbstractJob, Future<?>> futures;

	private JobDao dao;

	private int THREADS = 10;

	private Scheduler scheduler;

	private boolean persistent = true;

	private static final Log log = LogFactory.getLog(LongTimeQueue.class);

	public LongTimeQueue() {
		futures = new HashMap<AbstractJob, Future<?>>();
		queue = new Vector<AbstractJob>();
		scheduler = new Scheduler(THREADS);

		dao = new JobDao();

	}

	public void submit(AbstractJob job) {

		Future<?> future = scheduler.submit(job);
		futures.put(job, future);
		queue.add(job);
		log.info("Long Time Queue: Submit job...");
		save();

	}

	public void cancel(AbstractJob job) {

		if (job.getState() == AbstractJob.STATE_RUNNING
				|| job.getState() == AbstractJob.STATE_EXPORTING) {

			log.info("Long Time Queue: Cancel Job " + job.getId() + "...");

			job.kill();
			job.cancel();
			save();

		}

		if (job.getState() == AbstractJob.STATE_WAITING) {

			log.info("Long Time Queue: Cancel Job...");

			job.cancel();

			queue.remove(job);
			futures.remove(job);
			dao.insert(job);

			save();
		}

	}

	@Override
	public void run() {
		while (true) {

			List<AbstractJob> complete = new Vector<AbstractJob>();
			for (AbstractJob job : futures.keySet()) {
				Future<?> future = futures.get(job);
				if (future.isDone() || future.isCancelled()) {
					log.info("Long Time Queue: Job " + job.getId()
							+ ": finished");
					dao.insert(job);
					queue.remove(job);
					complete.add(job);
				}
			}

			for (AbstractJob job : complete) {
				futures.remove(job);
			}

			if (complete.size() > 0) {
				save();
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void pause() {
		log.info("Long Time Queue: Pause...");
		scheduler.pause();
	}

	public void resume() {
		log.info("Long Time Queue: Resume...");
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

	public int getPositionInQueue(AbstractJob job) {

		int index = queue.indexOf(job);

		if (index < 0) {
			return 0;
		} else {
			return index + 1 - scheduler.getActiveCount();
		}
	}

	protected void save() {

		if (persistent) {

			try {
				YamlWriter writer = new YamlWriter(new FileWriter(
						QUEUE_FILENAME));
				writer.write(getAllJobs());
				writer.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}
}
