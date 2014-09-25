package cloudgene.sample;

import java.io.File;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.mapred.ClusterStatus;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.jobs.Message;
import cloudgene.mapred.util.HadoopUtil;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlStep;

public class SampleStep extends CloudgeneStep {

	@Override
	public boolean run(WdlStep step, CloudgeneContext context) {

		Settings settings = Settings.getInstance();

		ok("<i>Please check below if we detected your cluster properly.</i>");

		ClusterStatus cluster = HadoopUtil.getInstance().getClusterDetails();
		StringBuffer state = new StringBuffer();
		state.append("<b>Hadoop Cluster</b>\n");
		state.append("State: " + cluster.getJobTrackerStatus().toString()
				+ "\n");
		state.append("MapTask: " + cluster.getMaxMapTasks() + "\n");
		state.append("ReduceTask: " + cluster.getMaxReduceTasks() + "\n");
		state.append("<b>Nodes</b>\n");
		for (String tracker : cluster.getActiveTrackerNames()) {
			state.append(tracker + "\n");
		}

		ok(state.toString());

		String hadoop = settings.getHadoopPath();

		if (new File(hadoop).exists()) {
			ok("Hadoop Binary was found in " + hadoop);
		} else {

			if (hadoop.trim().isEmpty()) {
				error("Hadoop Path was not set (set it here)");
			} else {
				error("Hadoop Binary was not found in  " + hadoop
						+ " (change it here)");
			}

			return false;
		}

		// TODO: write file to hdfs temp directory

		ok("HDFS File System check");

		// TODO: write file to local temp directory

		ok("Local File System check");

		// Mail Server....

		if (context.getUser().getMail() != null) {

			String subject = "Mail Server Test";
			String message = "This email was sent by Cloudgene to test your mail-server settings.";

			try {

				context.sendMail(subject, message);

				ok("We have sent an email to <b>" + context.getUser().getMail()
						+ "</b> with the password.");

			} catch (Exception e) {
				error("Sending mail failed: " + e.getMessage());
				return false;
			}

		} else {

			error("No email address found. Please enter your email address (Account -> Profile).");
			return false;

		}

		ok("Congratulations. Cloudgene works properly on your Hadoop Cluster!\nNow you can install applications (plese click here more details)");

		return true;

	}

}
