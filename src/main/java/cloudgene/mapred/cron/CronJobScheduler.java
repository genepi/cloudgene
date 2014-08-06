package cloudgene.mapred.cron;

import static org.quartz.CronScheduleBuilder.dailyAtHourAndMinute;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;

public class CronJobScheduler {

	public void start() throws SchedulerException {

		SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();

		Scheduler sched = schedFact.getScheduler();

		sched.start();

		// Reitre job every day at 1:00AM
		JobDetail jobRetire = newJob(RetireJob.class).withIdentity("retire",
				"jobs").build();
		runJobDailyAt(sched, "retire-trigger", jobRetire, 1, 00);

		// Notification job every day at 00:30AM
		JobDetail jobNotification = newJob(NotificationJob.class).withIdentity(
				"notification", "jobs").build();
		runJobDailyAt(sched, "notification-trigger", jobNotification, 0, 30);

	}

	private void runJobDailyAt(Scheduler scheduler, String triggerName,
			JobDetail job, int hour, int minutes) throws SchedulerException {
		Trigger trigger = newTrigger().withIdentity(triggerName, "jobs")
				.startNow().withSchedule(dailyAtHourAndMinute(hour, minutes))
				.build();
		scheduler.scheduleJob(job, trigger);
	}

}
