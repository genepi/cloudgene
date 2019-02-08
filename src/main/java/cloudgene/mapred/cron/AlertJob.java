package cloudgene.mapred.cron;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cloudgene.mapred.WebApp;
import cloudgene.mapred.util.MailUtil;
import genepi.hadoop.HadoopUtil;

public class AlertJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		WebApp application = (WebApp) dataMap.get("application");

		// check namenode state

		try {
		boolean safemode = HadoopUtil.getInstance().isInSafeMode();

		if (safemode) {

			if (application.getWorkflowEngine().isRunning()) {

				try {

					MailUtil.notifySlack(application.getSettings(), "Hi!\n\n" + "Your Hadoop cluster is in Safemode :scream:. "
							+ "Don't worry, I blocked the queue for you ;)");

					MailUtil.notifyAdmin(application.getSettings(),
							"[" + application.getSettings().getName() + "] Problems with your Hadoop cluster",
							"Hi,\n\n" + "This is a notification sent by Cloudgene.\n\n"
									+ "Your Hadoop cluster is in Safemode. "
									+ "Don't worry, we blocked the queue for you!");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				application.getWorkflowEngine().block();

			}

		}
		}catch (NoClassDefFoundError e) {
			// TODO: handle exception
		}

		// if (cluster.getJobTrackerStatus() == )

		// block queue if tasktracker is in safemode

		// send notification to admins

	}

}
