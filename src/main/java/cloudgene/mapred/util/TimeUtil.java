package cloudgene.mapred.util;

public class TimeUtil {

	public static String format(long time) {
		long h = (long) (Math.floor((time / 1000) / 60 / 60));
		long m = (long) ((Math.floor((time / 1000) / 60)) % 60);

		return (h > 0 ? h + " h " : "") + (m > 0 ? m + " min " : "") + (int) ((Math.floor(time / 1000)) % 60) + " sec";
	}

}
