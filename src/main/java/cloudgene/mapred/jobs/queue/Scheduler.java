package cloudgene.mapred.jobs.queue;

import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * A thin wrapper around a thread pool executor that only exposes partially what
 * the executor is doing. This is so that we don't make a mistake somewhere
 * along the way and jack something up.
 * 
 * @author Matthew A. Johnston (warmwaffles)
 */
public class Scheduler {
	private PausableThreadPoolExecutor executor;
	private LinkedBlockingQueue<Runnable> queue;

	public Scheduler(int threads) {
		queue = new LinkedBlockingQueue<Runnable>();
		executor = new PausableThreadPoolExecutor(threads, threads, 10,
				TimeUnit.SECONDS, queue);
	}

	public Future submit(Runnable runnable) {
		return executor.submit(runnable);
	}
	
	public void kill(Runnable runnable) {
		 executor.remove(runnable);
	}

	public boolean isRunning() {
		return executor.isRunning();
	}

	public int getActiveCount() {
		return executor.getActiveCount();
	}

	public void pause() {
		executor.pause();
	}

	public void resume() {
		executor.resume();
	}

	public void clear() {
		queue.clear();
	}

	public boolean isEmpty() {
		return queue.isEmpty();
	}
}