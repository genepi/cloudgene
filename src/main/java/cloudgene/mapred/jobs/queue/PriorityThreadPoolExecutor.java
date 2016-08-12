package cloudgene.mapred.jobs.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PriorityThreadPoolExecutor {
	private PausableThreadPoolExecutor executor;
	private BlockingQueue<Runnable> queue;

	private static final Log log = LogFactory.getLog(PriorityThreadPoolExecutor.class);

	public PriorityThreadPoolExecutor(int threads, boolean priority) {
		if (priority) {
			queue = new PriorityBlockingQueue<Runnable>();
		} else {
			queue = new LinkedBlockingQueue<Runnable>();

		}
		executor = new PausableThreadPoolExecutor(threads, threads, 10, TimeUnit.SECONDS, queue);
	}

	public Future submit(PriorityRunnable runnable) {
		return executor.submit(runnable);
	}

	public void kill(PriorityRunnable runnable) {
		executor.remove(runnable);
	}

	public Future resubmit(PriorityRunnable runnable) {
		executor.remove(runnable);
		Future future = executor.submit(runnable);
		return future;
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