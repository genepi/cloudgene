package cloudgene.mapred.cron;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.CronScheduleBuilder.dailyAtHourAndMinute;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;

import cloudgene.mapred.WebApp;
import cloudgene.mapred.util.Settings;

public class CronJobScheduler {

	private WebApp app;

	private Scheduler sched;

	public CronJobScheduler(WebApp app) {
		this.app = app;
	}

	public void start() throws SchedulerException {

		Settings settings = app.getSettings();

		SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();

		sched = schedFact.getScheduler();
		sched.start();

		if (settings.isAutoRetire()) {

			// Reitre job every 5 hours
			JobDetail jobRetire = newJob(RetireJob.class).withIdentity("retire", "jobs").build();
			jobRetire.getJobDataMap().put("application", app);
			Trigger trigger = newTrigger().withIdentity("retire-trigger", "jobs").startNow()
					.withSchedule(simpleSchedule().withIntervalInHours(settings.getAutoRetireInterval()) // every
																			// 5
																			// hours
							.repeatForever())
					.build();
			sched.scheduleJob(jobRetire, trigger);

			// Notification job every 5 hours
			JobDetail jobNotification = newJob(NotificationJob.class).withIdentity("notification", "jobs").build();
			jobNotification.getJobDataMap().put("application", app);
			Trigger trigger2 = newTrigger().withIdentity("notification-trigger", "jobs").startNow()
					.withSchedule(simpleSchedule().withIntervalInHours(settings.getAutoRetireInterval()) // every
																			// 5
																			// hours
							.repeatForever())
					.build();
			sched.scheduleJob(jobNotification, trigger2);

		}

		if (settings.isWriteStatistics()) {

			// Statistics (every 5 minutes)
			JobDetail statisticsJob = newJob(StatisticsJob.class).withIdentity("stats", "jobs").build();
			statisticsJob.getJobDataMap().put("application", app);
			Trigger trigger = newTrigger().withIdentity("stats-trigger", "jobs").startNow()
					.withSchedule(cronSchedule("0 0/5 * * * ?")).build();
			sched.scheduleJob(statisticsJob, trigger);

		}

		// Alerts (every 1 minutes)
		JobDetail alerJobDetail = newJob(AlertJob.class).withIdentity("alerts", "jobs").build();
		alerJobDetail.getJobDataMap().put("application", app);
		Trigger trigger = newTrigger().withIdentity("alert-trigger", "jobs").startNow()
				.withSchedule(cronSchedule("0 0/1 * * * ?")).build();
		sched.scheduleJob(alerJobDetail, trigger);

	}

	public void stop() throws SchedulerException {
		sched.shutdown();
	}
}
