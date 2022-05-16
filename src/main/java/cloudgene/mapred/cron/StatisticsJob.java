package cloudgene.mapred.cron;

import java.util.List;
import java.util.Map;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.CounterHistoryDao;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.server.Application;
import genepi.db.Database;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class StatisticsJob {

	@Inject
	protected Application application;
	
	@Scheduled(fixedDelay = "5m") 
	public void execute() {

		if (!application.getSettings().isWriteStatistics()) {
			return;
		}
		
		WorkflowEngine engine = application.getWorkflowEngine();
		Database database = application.getDatabase();

		List<AbstractJob> jobs = engine.getAllJobsInLongTimeQueue();
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

		Map<String, Long> countersRunning = engine
				.getCounters(AbstractJob.STATE_RUNNING);
		Map<String, Long> countersWaiting = engine
				.getCounters(AbstractJob.STATE_WAITING);
		Map<String, Long> countersComplete = engine
				.getCounters(AbstractJob.STATE_SUCCESS);

		UserDao daoUser = new UserDao(database);
		List<User> users = daoUser.findAll();

		long timestamp = System.currentTimeMillis();

		CounterHistoryDao daoHistory = new CounterHistoryDao(database);
		daoHistory.insert(timestamp, "users", users.size());
		daoHistory.insert(timestamp, "runningJobs", countRunning);
		daoHistory.insert(timestamp, "waitingJobs", countWaiting);
		daoHistory.insert(timestamp, "runningChromosomes",
				(countersRunning.get("chromosomes") == null ? 0
						: countersRunning.get("chromosomes")));
		daoHistory.insert(timestamp, "waitingChromosomes",
				(countersWaiting.get("chromosomes") == null ? 0
						: countersWaiting.get("chromosomes")));
		daoHistory.insert(timestamp, "completeChromosomes",
				(countersComplete.get("chromosomes") == null ? 0
						: countersComplete.get("chromosomes")));
		daoHistory.insert(timestamp, "completeJobs", (countersComplete
				.get("runs") == null ? 0 : countersComplete.get("runs")));

	}
}
