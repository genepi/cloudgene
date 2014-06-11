package cloudgene.mapred.util;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ParallelCalculation {

	private int threads = 8;

	private ArrayBlockingQueue<Runnable> queue;

	private ThreadPoolExecutor threadPool;

	public boolean run(List<Thread> threads) {
		long start = System.currentTimeMillis();

		queue = new ArrayBlockingQueue<Runnable>(100);

		threadPool = new ThreadPoolExecutor(this.threads, this.threads, 10,
				TimeUnit.SECONDS, queue);

		int tasks = threads.size();

		for (Thread thread : threads) {
			threadPool.execute(thread);
		}

		while (threadPool.getActiveCount() > 0) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		threadPool.shutdown();

		long elapsed = (System.currentTimeMillis() - start) / 1000;

		System.out.println("\n\n");
		System.out.println("Number of Threads: " + this.threads);
		System.out.println("Number of Tasks: " + tasks);
		System.out.println("Elapsed time: " + elapsed + " sec");
		System.out.println("\n\n");
		
		return true;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

}