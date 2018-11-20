package cloudgene.mapred.cli;

import org.apache.hadoop.mapred.ClusterStatus;

import cloudgene.mapred.util.HadoopCluster;
import genepi.hadoop.HadoopUtil;

public class VerifyCluster extends BaseTool {

	public VerifyCluster(String[] args) {
		super(args);
	}

	@Override
	public void createParameters() {
	}

	@Override
	public int run() {

		try {
			HadoopCluster.verifyCluster();

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
			System.out.println();

			return 0;

		} catch (Exception e) {
			System.out.println();
			printlnInRed(e.getMessage());
			System.out.println();
			return 1;
		}

	}
}