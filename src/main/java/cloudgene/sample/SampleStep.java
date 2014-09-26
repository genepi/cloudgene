package cloudgene.sample;

import genepi.io.FileUtil;

import java.io.File;

import org.apache.hadoop.mapred.ClusterStatus;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.util.HadoopUtil;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlStep;

public class SampleStep extends CloudgeneStep {

	@Override
	public boolean run(WdlStep step, CloudgeneContext context) {

		Settings settings = Settings.getInstance();

		ok("<i>Cloudgene runs with the following Hadoop configuration:</i>");

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

		String hadoopPath = settings.getHadoopPath();

		if (hadoopPath.trim().isEmpty()) {
			error("Hadoop Binary was not set. Please set the correct path in the admin panel.");
			return false;
		}

		File path = new File(hadoopPath);

		if (!path.exists()) {
			error("Hadoop Binary <code>"
					+ path
					+ "</code> was not found. Please set the correct path in the admin panel.");
			return false;
		}

		String hadoop = "";

		if (path.isDirectory()) {
			hadoop = FileUtil.path(hadoopPath, "bin", "hadoop");
		} else {
			hadoop = hadoopPath;
		}

		File file = new File(hadoop);

		if (!file.exists()) {
			error("Hadoop Binary <code>"
					+ hadoop
					+ "</code> was not found. Please set the correct path in the admin panel.");
			return false;
		}

		if (!file.canExecute()) {
			error("Hadoop Binary <code>"
					+ hadoop
					+ "</code> was found, but it can not be executed. Please check the permissions.");
			return false;
		}

		ok("Hadoop Binary was found in <code>" + hadoop
				+ "</code> and is executable.");

		
		// TODO: write r script which checks packages

		
		ok("R was found and all packages are installed.");

		
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

				ok("We have sent a test-email to <b>"
						+ context.getUser().getMail() + "</b>.");

			} catch (Exception e) {
				error("Sending mail failed: " + e.getMessage());
				return false;
			}

		} else {

			error("No email address found. Please enter your email address (Account -> Profile).");
			return false;

		}

		ok("<b>Congratulations.</b> Cloudgene works properly on your Hadoop Cluster!");

		return true;

	}

}
