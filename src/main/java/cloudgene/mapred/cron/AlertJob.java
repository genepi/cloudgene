package cloudgene.mapred.cron;

import cloudgene.mapred.server.Application;
import cloudgene.mapred.util.MailUtil;
import genepi.hadoop.HadoopUtil;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class AlertJob {

	@Inject
	protected Application application;

	@Scheduled(fixedDelay = "1m")
	public void execute() {

		// check namenode state

		try {
			boolean safemode = HadoopUtil.getInstance().isInSafeMode();

			if (safemode) {

				if (application.getWorkflowEngine().isRunning()) {

					try {

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
		} catch (NoClassDefFoundError e) {
			// TODO: handle exception
		}

		// if (cluster.getJobTrackerStatus() == )

		// block queue if tasktracker is in safemode

		// send notification to admins

	}

}
