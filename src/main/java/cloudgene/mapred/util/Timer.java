package cloudgene.mapred.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Timer {

	static long start = 0;

	private static final Log log = LogFactory.getLog(Timer.class);

	public static void start() {
		start = System.currentTimeMillis();
	}

	public static long stop(boolean verbose) {
		long time = System.currentTimeMillis() - start;
		if (!verbose) {
			log.info("Elapsed Time: " + time + "ms");
		}
		return time;
	}

	public static long stop() {
		return stop(false);
	}

}
