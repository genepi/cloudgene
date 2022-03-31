package cloudgene.mapred.cron;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cloudgene.mapred.server.Application;

public class NotificationJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		Application application = (Application)dataMap.get("application");		
		CleanUpTasks.sendNotifications(application);

	}


}
