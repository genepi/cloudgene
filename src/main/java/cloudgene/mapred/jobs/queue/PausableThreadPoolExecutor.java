package cloudgene.mapred.jobs.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A light wrapper around the {@link ThreadPoolExecutor}. It allows for you to
 * pause execution and resume execution when ready. It is very handy for games
 * that need to pause.
 * 
 * @author Matthew A. Johnston (warmwaffles)
 */
public class PausableThreadPoolExecutor extends ThreadPoolExecutor {
	private boolean isPaused;
	private ReentrantLock lock;
	private Condition condition;

	/**
	 * @param corePoolSize
	 *            The size of the pool
	 * @param maximumPoolSize
	 *            The maximum size of the pool
	 * @param keepAliveTime
	 *            The amount of time you wish to keep a single task alive
	 * @param unit
	 *            The unit of time that the keep alive time represents
	 * @param workQueue
	 *            The queue that holds your tasks
	 * @see {@link ThreadPoolExecutor#ThreadPoolExecutor(int, int, long, TimeUnit, BlockingQueue)}
	 */
	public PausableThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		lock = new ReentrantLock();
		condition = lock.newCondition();
	}

	/**
	 * @param thread
	 *            The thread being executed
	 * @param runnable
	 *            The runnable task
	 * @see {@link ThreadPoolExecutor#beforeExecute(Thread, Runnable)}
	 */
	@Override
	protected void beforeExecute(Thread thread, Runnable runnable) {
		super.beforeExecute(thread, runnable);
		lock.lock();
		try {
			while (isPaused)
				condition.await();
		} catch (InterruptedException ie) {
			thread.interrupt();
		} finally {
			lock.unlock();
		}
	}

	public boolean isRunning() {
		return !isPaused;
	}

	public boolean isPaused() {
		return isPaused;
	}

	/**
	 * Pause the execution
	 */
	public void pause() {
		lock.lock();
		try {
			isPaused = true;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Resume pool execution
	 */
	public void resume() {
		lock.lock();
		try {
			isPaused = false;
			condition.signalAll();
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
		return new ComparableFutureTask<T>(runnable, value);
	}
	
	protected class ComparableFutureTask<T> 
    extends FutureTask<T> implements Comparable<ComparableFutureTask<T>> {

        private Object object;

        public ComparableFutureTask(Runnable runnable, T result) {
            super(runnable, result);
            object = runnable;
        }

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public int compareTo(ComparableFutureTask<T> o) {
            if (this == o) {
                return 0;
            }
            if (o == null) {
                return -1; // this has higher priority than null
            }
            if (object != null && o.object != null) {
                if (object.getClass().equals(o.object.getClass())) {
                    if (object instanceof Comparable) {
                        return ((Comparable) object).compareTo(o.object);
                    }
                }
            }
            return 0;
        }
    }

}