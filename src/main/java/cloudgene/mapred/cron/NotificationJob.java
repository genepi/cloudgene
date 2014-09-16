package cloudgene.mapred.cron;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.MailUtil;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.util.Template;

public class NotificationJob implements Job {

	private static final Log log = LogFactory.getLog(NotificationJob.class);

	private String message = "";

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {

		Settings settings = Settings.getInstance();

		int days = settings.getRetireAfter() - settings.getNotificationAfter();

		JobDao dao = new JobDao();

		List<AbstractJob> oldJobs = dao
				.findAllOlderThan(settings.getNotificationAfterInSec(),
						AbstractJob.STATE_SUCCESS);

		int send = 0;

		for (AbstractJob job : oldJobs) {

			try {

				String subject = "[" + settings.getName() + "] Job "
						+ job.getName() + " will be retired in " + days
						+ " days";

				String body = settings.getTemplate(Template.RETIRE_JOB_MAIL,
						job.getUser().getFullName(), days, job.getName());

				MailUtil.send(settings, job.getUser().getMail(), subject, body);

				job.setState(AbstractJob.STATE_SUCESS_AND_NOTIFICATION_SEND);
				job.setDeletedOn(System.currentTimeMillis()
						+ ((settings.getRetireAfterInSec() - settings
								.getNotificationAfterInSec()) * 1000));

				log.info("Sent notification for job " + job.getId() + ".");
				send++;
				dao.update(job);

			} catch (Exception e) {

				log.error("Sent notification for job " + job.getId()
						+ " failed.", e);

			}

		}

		oldJobs = dao.findAllOlderThan(settings.getNotificationAfterInSec(),
				AbstractJob.STATE_FAILED);

		int otherJobs = 0;

		for (AbstractJob job : oldJobs) {

			log.info("Job failed, no notification sent for job " + job.getId()
					+ ".");
			job.setState(AbstractJob.STATE_FAILED_AND_NOTIFICATION_SEND);
			job.setDeletedOn(System.currentTimeMillis()
					+ ((settings.getRetireAfterInSec() - settings
							.getNotificationAfterInSec()) * 1000));
			dao.update(job);
			otherJobs++;

		}

		oldJobs = dao.findAllOlderThan(settings.getNotificationAfterInSec(),
				AbstractJob.STATE_CANCELED);

		for (AbstractJob job : oldJobs) {

			log.info("Job failed, no notification sent for job " + job.getId()
					+ ".");
			job.setState(AbstractJob.STATE_FAILED_AND_NOTIFICATION_SEND);
			job.setDeletedOn(System.currentTimeMillis()
					+ ((settings.getRetireAfterInSec() - settings
							.getNotificationAfterInSec()) * 1000));
			dao.update(job);
			otherJobs++;

		}

		log.info(send + " notifications sent. " + otherJobs
				+ " jobs marked without email ntofication.");

		message = (send + " notifications sent. " + otherJobs + " jobs marked without email ntofication.");

	}

	public String getMessage() {
		return message;
	}

}
