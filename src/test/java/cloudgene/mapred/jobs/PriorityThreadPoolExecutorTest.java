package cloudgene.mapred.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import cloudgene.mapred.TestApplication;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.jobs.workspace.WorkspaceFactory;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlReader;
import genepi.io.FileUtil;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;

@MicronautTest
public class PriorityThreadPoolExecutorTest {

	private static final int WAIT_FOR_CANCEL = 8000;

	@Inject
	TestApplication application;

	@Inject
	WorkspaceFactory workspaceFactory;
	
	@Test
	public void testCancelRunningJob() throws Exception {

		WorkflowEngine engine = application.getWorkflowEngine();
		
		while (engine.getAllJobsInLongTimeQueue().size() > 0 || engine.getAllJobsInShortTimeQueue().size() > 0) {
			Thread.sleep(6000);
		}
		
		WdlApp app = WdlReader.loadAppFromFile("test-data/long-sleep.yaml");

		Map<String, String> inputs = new HashMap<String, String>();
		inputs.put("input", "input-file");
		List<AbstractJob> jobsBeforeSubmit = engine.getAllJobsInLongTimeQueue();

		AbstractJob job1 = createJobFromWdl(app, "job_running", inputs);
		engine.submit(job1);
		while(job1.getState() == AbstractJob.STATE_WAITING) {
			Thread.sleep(1000);
		}

		assertEquals(AbstractJob.STATE_RUNNING, job1.getState());

		List<AbstractJob> jobsAfterSubmit = engine.getAllJobsInLongTimeQueue();
		assertEquals(jobsBeforeSubmit.size() + 1, jobsAfterSubmit.size());

		engine.cancel(job1);
		while(job1.getState() == AbstractJob.STATE_RUNNING) {
			Thread.sleep(1000);
		}
		assertEquals(AbstractJob.STATE_CANCELED, job1.getState());
		Thread.sleep(5000);
		
		List<AbstractJob> jobsAfterCancel = engine.getAllJobsInLongTimeQueue();
		assertEquals(jobsBeforeSubmit.size(), jobsAfterCancel.size());

		while (engine.getAllJobsInLongTimeQueue().size() > 0 || engine.getAllJobsInShortTimeQueue().size() > 0) {
			Thread.sleep(6000);
		}

	}

	@Test
	public void testCancelWaitingJob() throws Exception {
		
		WorkflowEngine engine = application.getWorkflowEngine();

		
		while (engine.getAllJobsInLongTimeQueue().size() > 0 || engine.getAllJobsInShortTimeQueue().size() > 0) {
			Thread.sleep(6000);
		}
		
		WdlApp app = WdlReader.loadAppFromFile("test-data/long-sleep.yaml");

		Map<String, String> inputs = new HashMap<String, String>();
		inputs.put("input", "input-file");
		List<AbstractJob> jobsBeforeSubmit = engine.getAllJobsInLongTimeQueue();

		AbstractJob job1 = createJobFromWdl(app, "job_running_a", inputs);
		engine.submit(job1);
		while(job1.getState() == AbstractJob.STATE_WAITING) {
			Thread.sleep(1000);
		}
		
		AbstractJob job2 = createJobFromWdl(app, "job_waiting_b", inputs);
		engine.submit(job2);

		Thread.sleep(5000);

		assertEquals(AbstractJob.STATE_RUNNING, job1.getState());
		assertEquals(AbstractJob.STATE_WAITING, job2.getState());

		List<AbstractJob> jobsAfterSubmit = engine.getAllJobsInLongTimeQueue();
		assertEquals(jobsBeforeSubmit.size() + 2, jobsAfterSubmit.size());

		engine.cancel(job2);
		while(job2.getState() == AbstractJob.STATE_RUNNING) {
			Thread.sleep(1000);
		}

		assertEquals(AbstractJob.STATE_CANCELED, job2.getState());

		List<AbstractJob> jobsAfterCancel = engine.getAllJobsInLongTimeQueue();
		assertEquals(jobsAfterSubmit.size() - 1, jobsAfterCancel.size());

		// clear queue
		engine.cancel(job1);

		while (engine.getAllJobsInLongTimeQueue().size() > 0 || engine.getAllJobsInShortTimeQueue().size() > 0) {
			Thread.sleep(6000);
		}
	}

	/**
	 * Submits 4 jobs, cancels job by jobs, checks states, priority and queue
	 * position
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMultipleJobs() throws Exception {

		WorkflowEngine engine = application.getWorkflowEngine();

		
		while (engine.getAllJobsInLongTimeQueue().size() > 0 || engine.getAllJobsInShortTimeQueue().size() > 0) {
			Thread.sleep(6000);
		}
		
		WdlApp app = WdlReader.loadAppFromFile("test-data/long-sleep.yaml");

		Map<String, String> inputs = new HashMap<String, String>();
		inputs.put("input", "input-file");

		AbstractJob job1 = createJobFromWdl(app, "job1", inputs);
		engine.submit(job1);

		Thread.sleep(5000);

		AbstractJob job2 = createJobFromWdl(app, "job2", inputs);
		engine.submit(job2);

		Thread.sleep(5000);

		AbstractJob job3 = createJobFromWdl(app, "job3", inputs);
		engine.submit(job3);

		Thread.sleep(5000);

		AbstractJob job4 = createJobFromWdl(app, "job4", inputs);
		engine.submit(job4);

		Thread.sleep(5000);

		assertEquals(AbstractJob.STATE_RUNNING, job1.getState());
		assertEquals(AbstractJob.STATE_WAITING, job2.getState());
		assertEquals(0, job2.getPositionInQueue());
		assertEquals(AbstractJob.STATE_WAITING, job3.getState());
		assertEquals(1, job3.getPositionInQueue());
		assertEquals(AbstractJob.STATE_WAITING, job4.getState());
		assertEquals(2, job4.getPositionInQueue());

		assertTrue(job1.getPriority() < job2.getPriority() && job2.getPriority() < job3.getPriority()
				&& job3.getPriority() < job4.getPriority());

		assertEquals(4, engine.getAllJobsInLongTimeQueue().size());

		// check if all jobs are sorted by priority
		List<AbstractJob> jobs = engine.getAllJobsInLongTimeQueue();
		assertEquals(0, jobs.indexOf(job1));
		assertEquals(1, jobs.indexOf(job2));
		assertEquals(2, jobs.indexOf(job3));
		assertEquals(3, jobs.indexOf(job4));

		engine.cancel(job1);
		Thread.sleep(WAIT_FOR_CANCEL);

		assertEquals(3, engine.getAllJobsInLongTimeQueue().size());
		assertEquals(AbstractJob.STATE_CANCELED, job1.getState());
		assertEquals(AbstractJob.STATE_RUNNING, job2.getState());
		assertEquals(AbstractJob.STATE_WAITING, job3.getState());
		assertEquals(0, job3.getPositionInQueue());
		assertEquals(AbstractJob.STATE_WAITING, job4.getState());
		assertEquals(1, job4.getPositionInQueue());

		engine.cancel(job2);
		Thread.sleep(WAIT_FOR_CANCEL);

		assertEquals(2, engine.getAllJobsInLongTimeQueue().size());
		assertEquals(AbstractJob.STATE_CANCELED, job1.getState());
		assertEquals(AbstractJob.STATE_CANCELED, job2.getState());
		assertEquals(AbstractJob.STATE_RUNNING, job3.getState());
		assertEquals(AbstractJob.STATE_WAITING, job4.getState());
		assertEquals(0, job4.getPositionInQueue());

		engine.cancel(job3);
		Thread.sleep(WAIT_FOR_CANCEL);

		assertEquals(1, engine.getAllJobsInLongTimeQueue().size());
		assertEquals(AbstractJob.STATE_CANCELED, job1.getState());
		assertEquals(AbstractJob.STATE_CANCELED, job2.getState());
		assertEquals(AbstractJob.STATE_CANCELED, job3.getState());
		assertEquals(AbstractJob.STATE_RUNNING, job4.getState());

		engine.cancel(job4);
		Thread.sleep(WAIT_FOR_CANCEL);

		assertEquals(0, engine.getAllJobsInLongTimeQueue().size());
		assertEquals(AbstractJob.STATE_CANCELED, job1.getState());
		assertEquals(AbstractJob.STATE_CANCELED, job2.getState());
		assertEquals(AbstractJob.STATE_CANCELED, job3.getState());
		assertEquals(AbstractJob.STATE_CANCELED, job4.getState());

		while (engine.getAllJobsInLongTimeQueue().size() > 0 || engine.getAllJobsInShortTimeQueue().size() > 0) {
			Thread.sleep(6000);
		}
	}

	/**
	 * Submits 4 jobs, job3 has small priority cancels job by jobs, checks
	 * states, priority and queue position
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMultipleJobsWithPriority() throws Exception {

		WorkflowEngine engine = application.getWorkflowEngine();

		
		while (engine.getAllJobsInLongTimeQueue().size() > 0 || engine.getAllJobsInShortTimeQueue().size() > 0) {
			Thread.sleep(6000);
		}
		
		WdlApp app = WdlReader.loadAppFromFile("test-data/long-sleep.yaml");

		Map<String, String> inputs = new HashMap<String, String>();
		inputs.put("input", "input-file");

		AbstractJob job1 = createJobFromWdl(app, "job1_a", inputs);
		engine.submit(job1);

		Thread.sleep(5000);

		AbstractJob job2 = createJobFromWdl(app, "job2_a", inputs);
		engine.submit(job2);

		Thread.sleep(5000);

		AbstractJob job3 = createJobFromWdl(app, "job3_a", inputs);
		engine.submit(job3);

		Thread.sleep(5000);

		AbstractJob job4 = createJobFromWdl(app, "job4_a", inputs);
		engine.submit(job4, 0);

		Thread.sleep(5000);

		assertEquals(AbstractJob.STATE_RUNNING, job1.getState());
		assertEquals(AbstractJob.STATE_WAITING, job2.getState());
		assertEquals(1, job2.getPositionInQueue());
		assertEquals(AbstractJob.STATE_WAITING, job3.getState());
		assertEquals(2, job3.getPositionInQueue());
		assertEquals(AbstractJob.STATE_WAITING, job4.getState());
		assertEquals(0, job4.getPositionInQueue());

		assertTrue(job1.getPriority() < job2.getPriority() && job2.getPriority() < job3.getPriority()
				&& job4.getPriority() < job2.getPriority() && job4.getPriority() < job3.getPriority());

		assertEquals(4, engine.getAllJobsInLongTimeQueue().size());

		// check if all jobs are sorted by priority
		List<AbstractJob> jobs = engine.getAllJobsInLongTimeQueue();
		assertEquals(0, jobs.indexOf(job1));
		assertEquals(2, jobs.indexOf(job2));
		assertEquals(3, jobs.indexOf(job3));
		assertEquals(1, jobs.indexOf(job4));

		engine.cancel(job1);
		Thread.sleep(WAIT_FOR_CANCEL);

		assertEquals(3, engine.getAllJobsInLongTimeQueue().size());
		assertEquals(AbstractJob.STATE_CANCELED, job1.getState());
		assertEquals(AbstractJob.STATE_WAITING, job2.getState());
		assertEquals(0, job2.getPositionInQueue());
		assertEquals(AbstractJob.STATE_WAITING, job3.getState());
		assertEquals(1, job3.getPositionInQueue());
		assertEquals(AbstractJob.STATE_RUNNING, job4.getState());

		engine.cancel(job4);
		Thread.sleep(WAIT_FOR_CANCEL);

		assertEquals(2, engine.getAllJobsInLongTimeQueue().size());
		assertEquals(AbstractJob.STATE_CANCELED, job1.getState());
		assertEquals(AbstractJob.STATE_RUNNING, job2.getState());
		assertEquals(AbstractJob.STATE_WAITING, job3.getState());
		assertEquals(AbstractJob.STATE_CANCELED, job4.getState());
		assertEquals(0, job3.getPositionInQueue());

		engine.cancel(job2);
		Thread.sleep(WAIT_FOR_CANCEL);

		assertEquals(1, engine.getAllJobsInLongTimeQueue().size());
		assertEquals(AbstractJob.STATE_CANCELED, job1.getState());
		assertEquals(AbstractJob.STATE_CANCELED, job2.getState());
		assertEquals(AbstractJob.STATE_RUNNING, job3.getState());
		assertEquals(AbstractJob.STATE_CANCELED, job4.getState());

		engine.cancel(job3);
		Thread.sleep(WAIT_FOR_CANCEL);

		assertEquals(0, engine.getAllJobsInLongTimeQueue().size());
		assertEquals(AbstractJob.STATE_CANCELED, job1.getState());
		assertEquals(AbstractJob.STATE_CANCELED, job2.getState());
		assertEquals(AbstractJob.STATE_CANCELED, job3.getState());
		assertEquals(AbstractJob.STATE_CANCELED, job4.getState());

		while (engine.getAllJobsInLongTimeQueue().size() > 0 || engine.getAllJobsInShortTimeQueue().size() > 0) {
			Thread.sleep(6000);
		}
	}

	/**
	 * Submits 4 jobs, job3 has small priority cancels job by jobs, checks
	 * states, priority and queue position
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMultipleJobsAndUpdatePriority() throws Exception {

		WorkflowEngine engine = application.getWorkflowEngine();

		
		while (engine.getAllJobsInLongTimeQueue().size() > 0 || engine.getAllJobsInShortTimeQueue().size() > 0) {
			Thread.sleep(6000);
		}
		
		WdlApp app = WdlReader.loadAppFromFile("test-data/long-sleep.yaml");

		Map<String, String> inputs = new HashMap<String, String>();
		inputs.put("input", "input-file");

		AbstractJob job1 = createJobFromWdl(app, "job1_ab", inputs);
		engine.submit(job1);

		Thread.sleep(5000);

		AbstractJob job2 = createJobFromWdl(app, "job2_ab", inputs);
		engine.submit(job2);

		Thread.sleep(5000);

		AbstractJob job3 = createJobFromWdl(app, "job3_ab", inputs);
		engine.submit(job3);

		Thread.sleep(5000);

		// submit with lowest priority
		AbstractJob job4 = createJobFromWdl(app, "job4_ab", inputs);
		engine.submit(job4);

		Thread.sleep(5000);

		assertEquals(AbstractJob.STATE_RUNNING, job1.getState());
		assertEquals(AbstractJob.STATE_WAITING, job2.getState());
		assertEquals(0, job2.getPositionInQueue());
		assertEquals(AbstractJob.STATE_WAITING, job3.getState());
		assertEquals(1, job3.getPositionInQueue());
		assertEquals(AbstractJob.STATE_WAITING, job4.getState());
		assertEquals(2, job4.getPositionInQueue());

		assertTrue(job1.getPriority() < job2.getPriority() && job2.getPriority() < job3.getPriority()
				&& job3.getPriority() < job4.getPriority());

		assertEquals(4, engine.getAllJobsInLongTimeQueue().size());

		// check if all jobs are sorted by priority
		List<AbstractJob> jobs = engine.getAllJobsInLongTimeQueue();
		assertEquals(0, jobs.indexOf(job1));
		assertEquals(1, jobs.indexOf(job2));
		assertEquals(2, jobs.indexOf(job3));
		assertEquals(3, jobs.indexOf(job4));

		// update priority to highest priority
		engine.updatePriority(job4, 0);

		Thread.sleep(5000);

		assertEquals(AbstractJob.STATE_RUNNING, job1.getState());
		assertEquals(AbstractJob.STATE_WAITING, job2.getState());
		assertEquals(1, job2.getPositionInQueue());
		assertEquals(AbstractJob.STATE_WAITING, job3.getState());
		assertEquals(2, job3.getPositionInQueue());
		assertEquals(AbstractJob.STATE_WAITING, job4.getState());
		assertEquals(0, job4.getPositionInQueue());

		assertTrue(job1.getPriority() < job2.getPriority() && job2.getPriority() < job3.getPriority()
				&& job4.getPriority() < job2.getPriority() && job4.getPriority() < job3.getPriority());

		assertEquals(4, engine.getAllJobsInLongTimeQueue().size());

		// check if all jobs are sorted by priority
		jobs = engine.getAllJobsInLongTimeQueue();
		assertEquals(0, jobs.indexOf(job1));
		assertEquals(2, jobs.indexOf(job2));
		assertEquals(3, jobs.indexOf(job3));
		assertEquals(1, jobs.indexOf(job4));

		engine.cancel(job1);
		Thread.sleep(WAIT_FOR_CANCEL);

		assertEquals(3, engine.getAllJobsInLongTimeQueue().size());
		assertEquals(AbstractJob.STATE_CANCELED, job1.getState());
		assertEquals(AbstractJob.STATE_WAITING, job2.getState());
		assertEquals(0, job2.getPositionInQueue());
		assertEquals(AbstractJob.STATE_WAITING, job3.getState());
		assertEquals(1, job3.getPositionInQueue());
		assertEquals(AbstractJob.STATE_RUNNING, job4.getState());

		engine.cancel(job4);
		Thread.sleep(WAIT_FOR_CANCEL);

		assertEquals(2, engine.getAllJobsInLongTimeQueue().size());
		assertEquals(AbstractJob.STATE_CANCELED, job1.getState());
		assertEquals(AbstractJob.STATE_RUNNING, job2.getState());
		assertEquals(AbstractJob.STATE_WAITING, job3.getState());
		assertEquals(AbstractJob.STATE_CANCELED, job4.getState());
		assertEquals(0, job3.getPositionInQueue());

		engine.cancel(job2);
		Thread.sleep(WAIT_FOR_CANCEL);

		assertEquals(1, engine.getAllJobsInLongTimeQueue().size());
		assertEquals(AbstractJob.STATE_CANCELED, job1.getState());
		assertEquals(AbstractJob.STATE_CANCELED, job2.getState());
		assertEquals(AbstractJob.STATE_RUNNING, job3.getState());
		assertEquals(AbstractJob.STATE_CANCELED, job4.getState());

		engine.cancel(job3);
		Thread.sleep(WAIT_FOR_CANCEL);

		assertEquals(0, engine.getAllJobsInLongTimeQueue().size());
		assertEquals(AbstractJob.STATE_CANCELED, job1.getState());
		assertEquals(AbstractJob.STATE_CANCELED, job2.getState());
		assertEquals(AbstractJob.STATE_CANCELED, job3.getState());
		assertEquals(AbstractJob.STATE_CANCELED, job4.getState());

		while (engine.getAllJobsInLongTimeQueue().size() > 0 || engine.getAllJobsInShortTimeQueue().size() > 0) {
			Thread.sleep(6000);
		}
	}

	@Test
	public CloudgeneJob createJobFromWdl(WdlApp app, String id, Map<String, String> inputs) throws Exception {

		UserDao userDao = new UserDao(application.getDatabase());				
		User user = userDao.findByUsername("user");
		
		Settings settings = application.getSettings();

		String localWorkspace = FileUtil.path(settings.getLocalWorkspace(), id);
		FileUtil.createDirectory(localWorkspace);

		CloudgeneJob job = new CloudgeneJob(user, id,app, inputs);
		job.setId(id);
		job.setName(id);
		job.setWorkspace(workspaceFactory.getDefault());
		job.setLocalWorkspace(localWorkspace);
		job.setSettings(settings);
		job.setApplication(app.getName() + " " + app.getVersion());
		job.setApplicationId(app.getId());

		return job;
	}

}
