package cloudgene.mapred.cli;

import org.apache.hadoop.mapred.ClusterStatus;

import cloudgene.mapred.util.HadoopCluster;
import cloudgene.mapred.util.Technology;
import genepi.base.Tool;
import genepi.hadoop.HadoopUtil;

public class VerifyCluster extends BaseTool {

	public static final String DEFAULT_HADOOP_USER = "cloudgene";

	public VerifyCluster(String[] args) {
		super(args);
	}

	@Override
	public void createParameters() {
		addOptionalParameter("user", "Hadoop username [default: " + DEFAULT_HADOOP_USER + "]", Tool.STRING);
		addOptionalParameter("conf", "Hadoop configuration folder", Tool.STRING);
	}

	@Override
	public int run() {

		if (getValue("conf") != null) {

			String conf = getValue("conf").toString();

			String username = null;
			if (getValue("user") != null) {
				username = getValue("user").toString();
			}
			System.out.println(
					"Use Haddop configuration folder " + conf + (username != null ? " with username " + username : ""));
			HadoopCluster.setConfPath("Unknown", conf, username);

		} else {
			if (settings.getCluster() == null) {
				System.out.println();
				printlnInRed("No external Haddop cluster set in cofiguration file");
			}
		}

		// print summary and warnigns
		if (settings.isEnable(Technology.HADOOP_CLUSTER)) {

			try {
				ClusterStatus cluster = HadoopUtil.getInstance().getClusterDetails();
				StringBuffer state = new StringBuffer();
				state.append("Mode: " + (HadoopUtil.getInstance().isInSafeMode() ? "Safe Mode" : "Running"));
				state.append("JobTracker: " + HadoopCluster.getJobTracker() + "\n");
				state.append("Default FS: " + HadoopCluster.getDefaultFS() + "\n");
				state.append("State: " + cluster.getJobTrackerStatus().toString() + "\n");
				state.append("MapTask: " + cluster.getMaxMapTasks() + "\n");
				state.append("ReduceTask: " + cluster.getMaxReduceTasks() + "\n");
				state.append("Nodes\n");
				for (String tracker : cluster.getActiveTrackerNames()) {
					state.append("  " + tracker + "\n");
				}
				state.append("Blacklist:\n");
				for (String tracker : cluster.getBlacklistedTrackerNames()) {
					state.append("  " + tracker + "\n");
				}
				System.out.println(state.toString());
				System.out.println();
				printlnInGreen("Hadoop cluster is ready to use.");
				
				//TODO: Test hdfs and user credentials
				
			} catch (Exception e) {
				System.out.println();
				printlnInRed("Hadoop cluster is unreachable.");
			}

		} else {
			printlnInRed("Hadoop cluster is unreachable. ");
		}

		return 0;

	}
}