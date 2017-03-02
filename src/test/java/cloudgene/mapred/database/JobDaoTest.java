package cloudgene.mapred.database;

import genepi.db.Database;

import java.util.List;

import org.junit.Test;

import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.util.junit.JobsApiTestCase;
import cloudgene.mapred.util.junit.TestServer;

public class JobDaoTest extends JobsApiTestCase {

	public static int DAYS_MS = 24 * 60 * 60 * 1000;

	public static int DAYS_SECONDS = 24 * 60 * 60;

	@Override
	protected void setUp() throws Exception {
		TestServer.getInstance().start();
	}

	@Test
	public void testFindAllOlderThan() throws Exception {

		// add 3 old jobs and one new

		Database database = TestServer.getInstance().getDatabase();

		UserDao userDao = new UserDao(database);
		User user = userDao.findByUsername("admin");

		JobDao jobDao = new JobDao(database);

		CloudgeneJob job1 = new CloudgeneJob();
		job1.setId("old-dummy-job-1-" + System.currentTimeMillis());
		job1.setName("old-dummy-job-1" + System.currentTimeMillis());
		job1.setState(CloudgeneJob.STATE_SUCCESS);
		job1.setStartTime(System.currentTimeMillis() - (8 * DAYS_MS));
		job1.setEndTime(System.currentTimeMillis() - (7 * DAYS_MS));
		job1.setUser(user);
		job1.setApplication("appplication");
		job1.setApplicationId("appplication-id");
		jobDao.insert(job1);

		CloudgeneJob job2 = new CloudgeneJob();
		job2.setId("old-dummy-job-2" + System.currentTimeMillis());
		job2.setName("old-dummy-job-2" + System.currentTimeMillis());
		job2.setState(CloudgeneJob.STATE_SUCCESS);
		job2.setStartTime(System.currentTimeMillis() - (10 * DAYS_MS));
		job2.setEndTime(System.currentTimeMillis() - (9 * DAYS_MS));
		job2.setUser(user);
		job2.setApplication("appplication");
		job2.setApplicationId("appplication-id");
		jobDao.insert(job2);

		CloudgeneJob job3 = new CloudgeneJob();
		job3.setId("old-dummy-job-3" + System.currentTimeMillis());
		job3.setName("old-dummy-job-3" + System.currentTimeMillis());
		job3.setState(CloudgeneJob.STATE_SUCCESS);
		job3.setStartTime(System.currentTimeMillis() - (9 * DAYS_MS));
		job3.setEndTime(System.currentTimeMillis() - (8 * DAYS_MS));
		job3.setUser(user);
		job3.setApplication("appplication");
		job3.setApplicationId("appplication-id");
		jobDao.insert(job3);

		CloudgeneJob job4 = new CloudgeneJob();
		job4.setId("old-dummy-job-4" + System.currentTimeMillis());
		job4.setName("old-dummy-job-4" + System.currentTimeMillis());
		job4.setState(CloudgeneJob.STATE_SUCCESS);
		job4.setStartTime(System.currentTimeMillis() - (3 * DAYS_MS));
		job4.setEndTime(System.currentTimeMillis() - (2 * DAYS_MS));
		job4.setUser(user);
		job4.setApplication("appplication");
		job4.setApplicationId("appplication-id");
		jobDao.insert(job4);

		System.out.println(jobDao.findAll().size());

		assertTrue(jobDao.findAll().size() > 3);

		List<AbstractJob> jobsOlder5Days = jobDao.findAllOlderThan(
				System.currentTimeMillis() - 5 * DAYS_MS,
				CloudgeneJob.STATE_SUCCESS);

		jobDao.delete(job1);
		jobDao.delete(job2);
		jobDao.delete(job3);
		jobDao.delete(job4);

		assertEquals(3, jobsOlder5Days.size());

	}

	@Test
	public void testFindAllByState() throws Exception {

		Database database = TestServer.getInstance().getDatabase();

		UserDao userDao = new UserDao(database);
		User user = userDao.findByUsername("admin");

		JobDao jobDao = new JobDao(database);

		CloudgeneJob job1 = new CloudgeneJob();
		job1.setId("old-dummy-job-1-" + System.currentTimeMillis());
		job1.setName("old-dummy-job-1" + System.currentTimeMillis());
		job1.setState(CloudgeneJob.STATE_FAILED);
		job1.setStartTime(System.currentTimeMillis() - (8 * DAYS_MS));
		job1.setEndTime(System.currentTimeMillis() - (7 * DAYS_MS));
		job1.setUser(user);
		job1.setApplication("appplication");
		job1.setApplicationId("appplication-id");
		jobDao.insert(job1);

		CloudgeneJob job2 = new CloudgeneJob();
		job2.setId("old-dummy-job-2" + System.currentTimeMillis());
		job2.setName("old-dummy-job-2" + System.currentTimeMillis());
		job2.setState(CloudgeneJob.STATE_FAILED);
		job2.setStartTime(System.currentTimeMillis() - (10 * DAYS_MS));
		job2.setEndTime(System.currentTimeMillis() - (9 * DAYS_MS));
		job2.setUser(user);
		job2.setApplication("appplication");
		job2.setApplicationId("appplication-id");
		jobDao.insert(job2);

		CloudgeneJob job3 = new CloudgeneJob();
		job3.setId("old-dummy-job-3" + System.currentTimeMillis());
		job3.setName("old-dummy-job-3" + System.currentTimeMillis());
		job3.setState(CloudgeneJob.STATE_FAILED);
		job3.setStartTime(System.currentTimeMillis() - (9 * DAYS_MS));
		job3.setEndTime(System.currentTimeMillis() - (8 * DAYS_MS));
		job3.setUser(user);
		job3.setApplication("appplication");
		job3.setApplicationId("appplication-id");
		jobDao.insert(job3);

		CloudgeneJob job4 = new CloudgeneJob();
		job4.setId("old-dummy-job-4" + System.currentTimeMillis());
		job4.setName("old-dummy-job-4" + System.currentTimeMillis());
		job4.setState(CloudgeneJob.STATE_SUCCESS);
		job4.setStartTime(System.currentTimeMillis() - (3 * DAYS_MS));
		job4.setEndTime(System.currentTimeMillis() - (2 * DAYS_MS));
		job4.setUser(user);
		job4.setApplication("appplication");
		job4.setApplicationId("appplication-id");
		jobDao.insert(job4);

		System.out.println(jobDao.findAll().size());

		assertTrue(jobDao.findAll().size() > 3);

		List<AbstractJob> failedJobs = jobDao
				.findAllByState(CloudgeneJob.STATE_FAILED);
		List<AbstractJob> succeedJobs = jobDao
				.findAllByState(CloudgeneJob.STATE_SUCCESS);

		jobDao.delete(job1);
		jobDao.delete(job2);
		jobDao.delete(job3);
		jobDao.delete(job4);

		assertTrue(failedJobs.size()  >= 3 );
		assertTrue(succeedJobs.size()  >= 1);
		
	}

}
