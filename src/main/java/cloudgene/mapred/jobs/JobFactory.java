package cloudgene.mapred.jobs;

public class JobFactory {

	public static AbstractJob create(int type) {

		switch (type) {
		case AbstractJob.TASK:

			return new TaskJob();

		case AbstractJob.MAPREDUCE:

			return new CloudgeneJob();

		default:
			return null;
		}

	}

}
