package cloudgene.mapred.cron;

import genepi.db.Database;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cloudgene.mapred.WebApp;
import cloudgene.mapred.util.Settings;

public class RetireJob implements Job {

	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {

		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		WebApp application = (WebApp) dataMap.get("application");
		Database database = application.getDatabase();
		Settings settings = application.getSettings();

		CleanUpTasks.executeRetire(database, settings);
	}

}
