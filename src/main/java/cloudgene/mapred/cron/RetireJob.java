package cloudgene.mapred.cron;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;

public class RetireJob implements Job {

	private static final Log log = LogFactory.getLog(RetireJob.class);

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {

		JobDao dao = new JobDao();

		List<AbstractJob> oldJobs = dao.findAllNotifiedJobs();

		int deleted = 0;
		for (AbstractJob job : oldJobs) {

			if (job.getDeletedOn() < System.currentTimeMillis()) {

				job.delete();

				job.setState(AbstractJob.STATE_RETIRED);
				dao.update(job);

				log.info("Job " + job.getId() + " retired.");
				deleted++;

			}

		}

		log.info(deleted + " jobs retired.");

	}

}
