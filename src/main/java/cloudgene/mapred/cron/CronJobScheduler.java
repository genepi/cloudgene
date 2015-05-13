package cloudgene.mapred.cron;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.CronScheduleBuilder.dailyAtHourAndMinute;
import static org.quartz.JobBuilder.newJob;
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

	public CronJobScheduler(WebApp app) {
		this.app = app;
	}

	public void start() throws SchedulerException {

		Settings settings = app.getSettings();

		SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();

		Scheduler sched = schedFact.getScheduler();
		sched.start();

		if (settings.isAutoRetire()) {

			// Reitre job every day at 1:00AM
			JobDetail jobRetire = newJob(RetireJob.class).withIdentity(
					"retire", "jobs").build();
			jobRetire.getJobDataMap().put("application", app);
			runJobDailyAt(sched, "retire-trigger", jobRetire, 1, 00);

			// Notification job every day at 00:30AM
			JobDetail jobNotification = newJob(NotificationJob.class)
					.withIdentity("notification", "jobs").build();
			jobNotification.getJobDataMap().put("application", app);
			runJobDailyAt(sched, "notification-trigger", jobNotification, 0, 30);

		}

		if (settings.isWriteStatistics()) {

			// Statistics (every 5 minutes)
			JobDetail statisticsJob = newJob(StatisticsJob.class).withIdentity(
					"stats", "jobs").build();
			statisticsJob.getJobDataMap().put("application", app);
			Trigger trigger = newTrigger()
					.withIdentity("stats-trigger", "jobs").startNow()
					.withSchedule(cronSchedule("0 0/5 * * * ?")).build();
			sched.scheduleJob(statisticsJob, trigger);

		}

	}

	private void runJobDailyAt(Scheduler scheduler, String triggerName,
			JobDetail job, int hour, int minutes) throws SchedulerException {
		Trigger trigger = newTrigger().withIdentity(triggerName, "jobs")
				.startNow().withSchedule(dailyAtHourAndMinute(hour, minutes))
				.build();
		scheduler.scheduleJob(job, trigger);
	}

}
