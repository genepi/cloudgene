package cloudgene.mapred.plugins.hadoop;

import cloudgene.mapred.plugins.IPlugin;
import cloudgene.mapred.util.Settings;
import genepi.hadoop.HadoopCluster;

public class HadoopPlugin implements IPlugin {

	public static final String ID = "hadoop";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "Hadoop Cluster";
	}

	@Override
	public boolean isInstalled() {
		try {
			if (HadoopCluster.verifyCluster()) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}

	}

	@Override
	public String getDetails() {
		StringBuffer state = new StringBuffer();
		state.append("JobTracker: " + HadoopCluster.getJobTracker() + "\n");
		state.append("Default FS: " + HadoopCluster.getDefaultFS() + "\n");
		state.append("State: " + HadoopCluster.getJobTrackerStatus().toString() + "\n");
		state.append("MapTask: " + HadoopCluster.getMaxMapTasks() + "\n");
		state.append("ReduceTask: " + HadoopCluster.getMaxReduceTasks() + "\n");
		state.append("Nodes\n");
		for (String tracker : HadoopCluster.getActiveTrackerNames()) {
			state.append("  " + tracker + "\n");
		}
		state.append("Blacklist:\n");
		for (String tracker : HadoopCluster.getBlacklistedTrackerNames()) {
			state.append("  " + tracker + "\n");
		}
		return state.toString();
	}

	@Override
	public void configure(Settings settings) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getStatus() {
		if (isInstalled()) {
			int nodes = HadoopCluster.getActiveTrackerNames().size();
			int mappTasks = HadoopCluster.getMaxMapTasks();
			int reduceTasks = HadoopCluster.getMaxReduceTasks();
			return "Cluster has " + nodes + " nodes, " + mappTasks + " map tasks and " + reduceTasks + " reduce tasks";
		} else {
			try {
				HadoopCluster.verifyCluster();
				return "Hadoop support disabled.";
			} catch (Exception e) {
				return "Hadoop support disabled. " + e.getMessage();
			}
		}
	}

}
