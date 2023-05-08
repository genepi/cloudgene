package cloudgene.mapred.server.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloudgene.mapred.core.Template;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.workspace.ExternalWorkspaceFactory;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.util.MailUtil;
import cloudgene.mapred.util.Settings;
import cloudgene.sdk.internal.IExternalWorkspace;
import genepi.db.Database;
import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class JobCleanUpService {

	private static final Logger log = LoggerFactory.getLogger(JobCleanUpService.class);

	@Inject
	protected Application application;

	public int executeRetire() {

		Database database = application.getDatabase();
		Settings settings = application.getSettings();

		JobDao dao = new JobDao(database);

		List<AbstractJob> oldJobs = dao.findAllNotifiedJobs();

		int deleted = 0;

		IExternalWorkspace externalWorkspace = null;
		if (!settings.getExternalWorkspaceLocation().isEmpty()) {
			String externalOutput = settings.getExternalWorkspaceLocation();
			externalWorkspace = ExternalWorkspaceFactory.get(settings.getExternalWorkspaceType(), externalOutput);
		}

		for (AbstractJob job : oldJobs) {

			if (job.getDeletedOn() < System.currentTimeMillis()) {

				// delete local directory and hdfs directory
				String localOutput = FileUtil.path(settings.getLocalWorkspace(), job.getId());
				FileUtil.deleteDirectory(localOutput);

				try {
					String hdfsOutput = HdfsUtil.makeAbsolute(HdfsUtil.path(settings.getHdfsWorkspace(), job.getId()));
					HdfsUtil.delete(hdfsOutput);
				} catch (NoClassDefFoundError e) {
					// TODO: handle exception
				}

				job.setState(AbstractJob.STATE_RETIRED);
				dao.update(job);

				log.info("Job " + job.getId() + " retired.");
				deleted++;

				if (externalWorkspace != null) {
					try {
						externalWorkspace.delete(job.getId());
					} catch (Exception e) {
						log.error("Retire " + job.getId() + " failed.", e);
					}
				}

			}

		}

		log.info(deleted + " jobs retired.");
		return deleted;
	}

	// TODO: duplicate code!
	public String sendNotification(AbstractJob job, int days) {

		int daysInMilliSeconds = days * 24 * 60 * 60 * 1000;

		Settings settings = application.getSettings();
		JobDao dao = new JobDao(application.getDatabase());

		if (job.getState() == AbstractJob.STATE_SUCCESS) {

			try {

				String subject = "[" + settings.getName() + "] Job " + job.getId() + " will be retired in " + days
						+ " days";

				String body = application.getTemplate(Template.RETIRE_JOB_MAIL, job.getUser().getFullName(), days,
						job.getId());

				MailUtil.send(settings, job.getUser().getMail(), subject, body);

				job.setState(AbstractJob.STATE_SUCESS_AND_NOTIFICATION_SEND);
				job.setDeletedOn(System.currentTimeMillis() + daysInMilliSeconds);
				dao.update(job);

				return "Sent notification for job " + job.getId() + ".";

			} catch (Exception e) {

				return "Sent notification for job " + job.getId() + " failed.";
			}

		} else if (job.getState() == AbstractJob.STATE_FAILED || job.getState() == AbstractJob.STATE_CANCELED) {

			job.setState(AbstractJob.STATE_FAILED_AND_NOTIFICATION_SEND);
			job.setDeletedOn(System.currentTimeMillis() + daysInMilliSeconds);
			dao.update(job);

			return job.getId() + ": delete date set. job failed, no notification sent.";

		} else {

			return "Job " + job.getId() + " has wrong state for this operation.";
		}
	}

	// TODO: reuse sendNotification(job)
	public int sendNotifications() {

		Database database = application.getDatabase();
		Settings settings = application.getSettings();

		int days = settings.getRetireAfter() - settings.getNotificationAfter();

		JobDao dao = new JobDao(database);

		List<AbstractJob> oldJobs = dao.findAllOlderThan(
				System.currentTimeMillis() - settings.getNotificationAfterInSec() * 1000, AbstractJob.STATE_SUCCESS);

		int send = 0;

		for (AbstractJob job : oldJobs) {

			try {

				String subject = "[" + settings.getName() + "] Job " + job.getId() + " will be retired in " + days
						+ " days";

				String body = application.getTemplate(Template.RETIRE_JOB_MAIL, job.getUser().getFullName(), days,
						job.getId());

				if (!job.getUser().getUsername().equals("public")) {

					MailUtil.send(settings, job.getUser().getMail(), subject, body);

				}

				job.setState(AbstractJob.STATE_SUCESS_AND_NOTIFICATION_SEND);
				job.setDeletedOn(System.currentTimeMillis()
						+ ((settings.getRetireAfterInSec() - settings.getNotificationAfterInSec()) * 1000));

				log.info("Sent notification for job " + job.getId() + ".");
				send++;
				dao.update(job);

			} catch (Exception e) {

				log.error("Sent notification for job " + job.getId() + " failed.", e);

			}

		}

		oldJobs = dao.findAllOlderThan(System.currentTimeMillis() - settings.getNotificationAfterInSec() * 1000,
				AbstractJob.STATE_FAILED);

		int otherJobs = 0;

		for (AbstractJob job : oldJobs) {

			log.info("Job failed, no notification sent for job " + job.getId() + ".");
			job.setState(AbstractJob.STATE_FAILED_AND_NOTIFICATION_SEND);
			job.setDeletedOn(System.currentTimeMillis()
					+ ((settings.getRetireAfterInSec() - settings.getNotificationAfterInSec()) * 1000));
			dao.update(job);
			otherJobs++;

		}

		oldJobs = dao.findAllOlderThan(System.currentTimeMillis() - settings.getNotificationAfterInSec() * 1000,
				AbstractJob.STATE_CANCELED);

		for (AbstractJob job : oldJobs) {

			log.info("Job failed, no notification sent for job " + job.getId() + ".");
			job.setState(AbstractJob.STATE_FAILED_AND_NOTIFICATION_SEND);
			job.setDeletedOn(System.currentTimeMillis()
					+ ((settings.getRetireAfterInSec() - settings.getNotificationAfterInSec()) * 1000));
			dao.update(job);
			otherJobs++;

		}

		log.info(send + " notifications sent. " + otherJobs + " jobs marked without email notification.");

		return send;

	}

}
