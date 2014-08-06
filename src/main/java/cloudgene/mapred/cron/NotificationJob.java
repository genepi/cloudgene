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

public class NotificationJob implements Job {

	private static final Log log = LogFactory.getLog(NotificationJob.class);

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {

		Settings settings = Settings.getInstance();

		JobDao dao = new JobDao();

		List<AbstractJob> oldJobs = dao
				.findAllOlderThan(settings.getNotificationAfterInSec(),
						AbstractJob.STATE_SUCCESS);

		for (AbstractJob job : oldJobs) {

			String subject = "Job " + job.getName()
					+ " will be retired in 2 days";

			String body = "Dear "
					+ job.getUser().getFullName()
					+ ",\nYour job will be retired in 2 days! All imputation results will be deleted at that time.\n\n"
					+ "Please ensure that you have downloaded all results from https://imputationserver.sph.umich.edu/start.html#!jobs/"
					+ job.getName();

			try {

				MailUtil.send(settings.getMail().get("smtp"), settings
						.getMail().get("port"), settings.getMail().get("user"),
						settings.getMail().get("password"), settings.getMail()
								.get("name"), job.getUser().getMail(), "["
								+ settings.getName() + "] " + subject, body);

				job.setState(AbstractJob.STATE_SUCESS_AND_NOTIFICATION_SEND);
				dao.update(job);

				log.info("Sent notification for job " + job.getId() + ".");

			} catch (Exception e) {

				log.error("Sent notification for job " + job.getId()
						+ " failed.", e);

			}

		}

		log.info(oldJobs.size() + " notifications sent.");

	}

}
