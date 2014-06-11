package cloudgene.mapred.util;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.apache.hadoop.mapred.ClusterStatus;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.JobStatus;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapred.TaskReport;
import org.apache.hadoop.util.VersionInfo;

import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.WorkflowEngine;

public class HadoopUtil {

	static HadoopUtil instance = null;

	private JobClient client = null;

	public static HadoopUtil getInstance() {
		if (instance == null) {
			instance = new HadoopUtil();
		}
		return instance;
	}

	private HadoopUtil() {
		JobConf job = new JobConf(WorkflowEngine.class);
		try {
			client = new JobClient(job);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void killAll(User user) {
		try {
			if (client.getAllJobs() != null) {
				for (JobStatus s : client.getAllJobs()) {

					if (s.getRunState() == JobStatus.RUNNING
							|| s.getRunState() == JobStatus.PREP) {

						RunningJob job = client.getJob(s.getJobID());
						job.killJob();

					}

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<JobStatus> getRunningJobs(User user) {

		List<JobStatus> result = new Vector<JobStatus>();
		try {

			if (client.getAllJobs() != null) {
				for (JobStatus s : client.getAllJobs()) {

					if (s.getRunState() == JobStatus.RUNNING
							|| s.getRunState() == JobStatus.PREP) {
						result.add(s);
					}
				}

			}

			return result;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;

	}

	public void kill(String id) throws IOException {
		RunningJob job = getJob(id);
		job.killJob();
	}

	public RunningJob getJob(String id) {

		RunningJob result = null;
		try {

			result = client.getJob(id);
			return result;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;

	}

	public TaskReport[] getMapperByJob(String id) {

		TaskReport[] result = null;
		try {

			result = client.getMapTaskReports(id);
			return result;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;

	}

	public TaskReport[] getReducerByJob(String id) {

		TaskReport[] result = null;
		try {
			result = client.getReduceTaskReports(id);
			return result;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	public ClusterStatus getClusterDetails() {

		try {

			return client.getClusterStatus(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	public String getVersion() {

		return VersionInfo.getVersion();

	}

}
