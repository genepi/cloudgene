package cloudgene.mapred.jobs.queue;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.AbstractJob;

public abstract class Queue implements Runnable {

	private List<AbstractJob> queue;

	private HashMap<AbstractJob, Future<?>> futures;

	private HashMap<AbstractJob, Runnable> runnables;
	
	private Scheduler scheduler;

	private String name = "";

	private static final Log log = LogFactory.getLog(Queue.class);

	public Queue(String name, int threads) {
		futures = new HashMap<AbstractJob, Future<?>>();
		runnables = new HashMap<AbstractJob, Runnable>();
		queue = new Vector<AbstractJob>();
		scheduler = new Scheduler(threads);
		this.name = name;
	}

	public void submit(AbstractJob job) {

		synchronized (futures) {

			synchronized (queue) {

				Runnable runnable = createRunnable(job);
				runnables.put(job, runnable);
				
				
				Future<?> future = scheduler.submit(runnable);
				futures.put(job, future);
				queue.add(job);
				log.info(name + ": Submit job...");

			}

		}

	}

	public void cancel(AbstractJob job) {

		if (job.getState() == AbstractJob.STATE_RUNNING
				|| job.getState() == AbstractJob.STATE_EXPORTING) {

			log.info(name + ": Cancel Job " + job.getId() + "...");

			job.kill();
			job.cancel();

		}

		if (job.getState() == AbstractJob.STATE_WAITING) {

			synchronized (futures) {

				synchronized (queue) {
					Runnable runnable = runnables.get(job);
					if (runnable != null){
						scheduler.kill(runnable);
					}
					job.cancel();
					queue.remove(job);
					futures.remove(job);
					onComplete(job);
					log.info(name + ": Cancel Job...");
				}

			}
		}

	}

	@Override
	public void run() {

		List<AbstractJob> complete = new Vector<AbstractJob>();

		while (true) {
			try {

				synchronized (futures) {

					synchronized (queue) {

						complete.clear();

						for (AbstractJob job : futures.keySet()) {
							Future<?> future = futures.get(job);
							if (future.isDone() || future.isCancelled()) {
								log.info(name + ": Job " + job.getId()
										+ ": finished");
								queue.remove(job);
								complete.add(job);
							}

						}

						for (AbstractJob job : complete) {
							onComplete(job);
							futures.remove(job);
						}

					}
				}

				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			} catch (Exception e) {

				log.warn(name + ": Concurrency Exception!! ");

			}

		}
	}

	public void pause() {
		log.info(name + ": Pause...");
		scheduler.pause();
	}

	public void resume() {
		log.info(name + ": Resume...");
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

	public int getPositionInQueue(AbstractJob job) {

		synchronized (queue) {

			int index = queue.indexOf(job);

			if (index < 0) {
				return 0;
			} else {
				return index + 1 - scheduler.getActiveCount();
			}

		}
	}

	public boolean isInQueue(AbstractJob job) {

		synchronized (queue) {

			return queue.contains(job);

		}
	}

	abstract public void onComplete(AbstractJob job);

	abstract public Runnable createRunnable(AbstractJob job);

}