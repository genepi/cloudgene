package cloudgene.mapred.cron;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.CounterDao;
import cloudgene.mapred.database.CounterHistoryDao;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.WorkflowEngine;

public class StatisticsJob implements Job {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"yy-MM-dd HH:mm");

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {

		List<AbstractJob> jobs = WorkflowEngine.getInstance()
				.getAllJobsInLongTimeQueue();
		long countWaiting = 0;
		long countRunning = 0;

		for (AbstractJob job : jobs) {
			if (job.getState() == AbstractJob.STATE_RUNNING) {
				countRunning++;
			}
			if (job.getState() == AbstractJob.STATE_WAITING) {
				countWaiting++;
			}
		}

		Map<String, Long> countersRunning = WorkflowEngine.getInstance()
				.getCounters(AbstractJob.STATE_RUNNING);
		Map<String, Long> countersWaiting = WorkflowEngine.getInstance()
				.getCounters(AbstractJob.STATE_WAITING);

		CounterDao dao = new CounterDao();
		Map<String, Long> counters = dao.getAll();

		UserDao daoUser = new UserDao();
		List<User> users = daoUser.findAll();

		long timestamp = System.currentTimeMillis();

		CounterHistoryDao daoHistory = new CounterHistoryDao();
		daoHistory.insert(timestamp, "users", users.size());
		daoHistory.insert(timestamp, "runningJobs", countRunning);
		daoHistory.insert(timestamp, "waitingJobs", countWaiting);
		daoHistory.insert(timestamp, "runningChromosomes",
				(countersRunning.get("chromosomes") == null ? 0
						: countersRunning.get("chromosomes")));
		daoHistory.insert(timestamp, "waitingChromosomes",
				(countersWaiting.get("chromosomes") == null ? 0
						: countersWaiting.get("chromosomes")));
		daoHistory.insert(timestamp, "completeChromosomes", (counters
				.get("chromosomes") == null ? 0 : counters.get("chromosomes")));

	}
}
