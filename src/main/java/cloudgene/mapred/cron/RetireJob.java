package cloudgene.mapred.cron;

import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.util.Settings;

public class RetireJob implements Job {

	private static final Log log = LogFactory.getLog(RetireJob.class);

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {

		Settings settings = Settings.getInstance();

		JobDao dao = new JobDao();

		List<AbstractJob> oldJobs = dao.findAllOlderThan(settings
				.getRetireAfterInSec());

		for (AbstractJob job : oldJobs) {
			job.cleanUp();

			job.setState(AbstractJob.STATE_RETIRED);
			dao.update(job);

			log.info("Job " + job.getId() + " retired.");

		}

		log.info(oldJobs.size() + " jobs retired.");

	}

}
