package cloudgene.mapred.cron;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cloudgene.mapred.server.Application;
import cloudgene.mapred.util.Settings;
import genepi.db.Database;

public class RetireJob implements Job {

	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {

		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		Application application = (Application) dataMap.get("application");
		Database database = application.getDatabase();
		Settings settings = application.getSettings();

		CleanUpTasks.executeRetire(database, settings);
	}

}
