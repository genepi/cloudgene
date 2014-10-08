package cloudgene.sample;

import genepi.hadoop.common.WorkflowContext;
import genepi.hadoop.common.WorkflowStep;
import genepi.io.FileUtil;

import java.io.File;

import org.apache.hadoop.mapred.ClusterStatus;

import cloudgene.mapred.util.HadoopUtil;
import cloudgene.mapred.util.Settings;

public class SampleStep extends WorkflowStep {

	@Override
	public boolean run(WorkflowContext context) {

		Settings settings = Settings.getInstance();

		context.ok("<i>Cloudgene runs with the following Hadoop configuration:</i>");

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

		context.ok(state.toString());

		String hadoopPath = settings.getHadoopPath();

		if (hadoopPath.trim().isEmpty()) {
			context.error("Hadoop Binary was not set. Please set the correct path in the admin panel.");
			return false;
		}

		File path = new File(hadoopPath);

		if (!path.exists()) {
			context.error("Hadoop Binary <code>"
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
			context.error("Hadoop Binary <code>"
					+ hadoop
					+ "</code> was not found. Please set the correct path in the admin panel.");
			return false;
		}

		if (!file.canExecute()) {
			context.error("Hadoop Binary <code>"
					+ hadoop
					+ "</code> was found, but it can not be executed. Please check the permissions.");
			return false;
		}

		context.ok("Hadoop Binary was found in <code>" + hadoop
				+ "</code> and is executable.");

		// TODO: write r script which checks packages

		context.ok("R was found and all packages are installed.");

		// TODO: write file to hdfs temp directory

		context.ok("HDFS File System check");

		// TODO: write file to local temp directory

		context.ok("Local File System check");

		// Mail Server....
		String mail = context.get("cloudgene.user.mail");
		if (mail != null) {

			String subject = "Mail Server Test";
			String message = "This email was sent by Cloudgene to test your mail-server settings.";

			try {

				context.sendMail(subject, message);

				context.ok("We have sent a test-email to <b>" + mail + "</b>.");

			} catch (Exception e) {
				context.error("Sending mail failed: " + e.getMessage());
				return false;
			}

		} else {

			context.error("No email address found. Please enter your email address (Account -> Profile).");
			return false;

		}

		context.ok("<b>Congratulations.</b> Cloudgene works properly on your Hadoop Cluster!");

		return true;

	}

}
