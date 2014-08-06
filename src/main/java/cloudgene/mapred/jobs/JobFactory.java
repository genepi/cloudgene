package cloudgene.mapred.jobs;

public class JobFactory {

	public static AbstractJob create(int type) {

		switch (type) {
		case AbstractJob.TYPE_TASK:

			return new TaskJob();

		case AbstractJob.TYPE_MAPREDUCE:

			return new CloudgeneJob();

		default:
			return null;
		}

	}

}
