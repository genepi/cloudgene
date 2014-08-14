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

public class CronJobScheduler {

	public void start(boolean autoRetire, boolean writeStatistics)
			throws SchedulerException {

		SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();

		Scheduler sched = schedFact.getScheduler();

		sched.start();

		if (autoRetire) {

			// Reitre job every day at 1:00AM
			JobDetail jobRetire = newJob(RetireJob.class).withIdentity(
					"retire", "jobs").build();
			runJobDailyAt(sched, "retire-trigger", jobRetire, 1, 00);

			// Notification job every day at 00:30AM
			JobDetail jobNotification = newJob(NotificationJob.class)
					.withIdentity("notification", "jobs").build();
			runJobDailyAt(sched, "notification-trigger", jobNotification, 0, 30);

		}

		if (writeStatistics) {

			// Statistics (every 5 minutes)
			JobDetail statisticsJob = newJob(StatisticsJob.class).withIdentity(
					"stats", "jobs").build();
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
