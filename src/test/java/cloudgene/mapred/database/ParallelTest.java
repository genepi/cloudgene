package cloudgene.mapred.database;

import genepi.db.Database;

import java.util.List;

import org.junit.Test;

import cloudgene.mapred.api.v2.JobsApiTestCase;
import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.util.junit.TestServer;

public class ParallelTest extends JobsApiTestCase {

	public static int DAYS_MS = 24 * 60 * 60 * 1000;

	public static int DAYS_SECONDS = 24 * 60 * 60;

	@Override
	protected void setUp() throws Exception {
		TestServer.getInstance().start();
	}

	@Test
	public void testFindAllJobs() throws Exception {

		final Database database = TestServer.getInstance().getDatabase();

		for (int i = 0; i < 1000; i++) {
			final int l = i;
			new Thread(new Runnable() {

				@Override
				public void run() {
					JobDao dao = new JobDao(database);
					List<AbstractJob> jobs = dao.findAll();
					System.out.println("request " + l +" done. result: " + jobs.size());
				}
			}).start();
		}
		Thread.sleep(100000);

	}
	
	@Test
	public void testFindAllCounterHistory() throws Exception {

		final Database database = TestServer.getInstance().getDatabase();

		for (int i = 0; i < 1000; i++) {
			final int l = i;
			new Thread(new Runnable() {

				@Override
				public void run() {
					CounterHistoryDao dao = new CounterHistoryDao(database);
					dao.getAllBeetween( System.currentTimeMillis() , System.currentTimeMillis());
					System.out.println("request " + l +" done. result: " + 1);
				}
			}).start();
		}
		Thread.sleep(100000);

	}

}
