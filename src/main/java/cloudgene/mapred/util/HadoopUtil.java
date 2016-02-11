package cloudgene.mapred.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FsStatus;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.mapred.ClusterStatus;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.JobStatus;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapred.TaskCompletionEvent;
import org.apache.hadoop.mapred.TaskLog;
import org.apache.hadoop.mapred.TaskReport;
import org.apache.hadoop.mapred.JobID;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.VersionInfo;

import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.WorkflowEngine;

public class HadoopUtil {

	static HadoopUtil instance = null;

	private JobClient client = null;

	private static final org.apache.commons.logging.Log log = LogFactory
			.getLog(HadoopUtil.class);

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
			log.error("JobClient init failed.", e);
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
			log.error("Kill All failed.", e);
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
			log.error("get Running jobs failed.", e);

		}

		return result;

	}

	public void kill(String id) throws IOException {
		RunningJob job = getJob(id);
		job.killJob();
	}

	/*
	 * public RunningJob getJob(String id) { RunningJob result = null; try {
	 * result = client.getJob(id); return result;
	 * 
	 * } catch (IOException e) { log.error("Get Job failed.", e);
	 * 
	 * }
	 * 
	 * return result;
	 * 
	 * }
	 */

	public RunningJob getJob(String id) {
		RunningJob result = null;
		try {
			JobStatus[] activeJobs = client.getAllJobs();

			for (JobStatus js : activeJobs) {
				if (js.getJobID().equals(JobID.forName(id))) {
					result = client.getJob(js.getJobID());
					break;
				}
			}

			return result;

		} catch (IOException e) {
			log.error("Get Job failed.", e);

		}

		return result;

	}

	public TaskReport[] getMapperByJob(String id) {

		TaskReport[] result = null;
		try {

			result = client.getMapTaskReports(id);
			return result;

		} catch (IOException e) {
			log.error("Get Mapper failed.", e);
		}

		return result;

	}

	public TaskReport[] getReducerByJob(String id) {

		TaskReport[] result = null;
		try {
			result = client.getReduceTaskReports(id);
			return result;

		} catch (IOException e) {
			log.error("Get Reducer failed.", e);
		}

		return result;
	}

	public ClusterStatus getClusterDetails() {

		try {

			return client.getClusterStatus(true);
		} catch (IOException e) {
			log.error("Get cluster details failed.", e);
			return null;
		}

	}

	public boolean isInSafeMode() {
		try {
			FileSystem fs = client.getFs();

			if (fs instanceof DistributedFileSystem) {
				DistributedFileSystem dfs = (DistributedFileSystem) fs;
				FsStatus ds = dfs.getStatus();
				return dfs.isInSafeMode();

			} else {
				return false;
			}
		} catch (IOException e) {
			log.error("Get safe mode failed.", e);
			return false;
		}
	}

	public String getVersion() {

		return VersionInfo.getVersion();

	}

	public void downloadFailedLogs(RunningJob runningJob, String folder) {

		log.info("Downloading events...");

		List<TaskCompletionEvent> completionEvents = new LinkedList<TaskCompletionEvent>();
		try {
			while (true) {
				TaskCompletionEvent[] bunchOfEvents;
				bunchOfEvents = runningJob.getTaskCompletionEvents(0);
				if (bunchOfEvents == null || bunchOfEvents.length == 0) {
					break;
				}
				completionEvents.addAll(Arrays.asList(bunchOfEvents));

			}

		} catch (Exception e) {
			log.error("Downloading events failed.", e);
			return;
		}

		log.info("Downloaded " + completionEvents.size() + " events.");

		log.info("Downloading " + completionEvents.size() + " log files...");
		for (TaskCompletionEvent taskCompletionEvent : completionEvents) {

			/*
			 * if (taskCompletionEvent.getStatus() == Status.FAILED) {
			 * 
			 * StringBuilder logURL = new StringBuilder(
			 * taskCompletionEvent.getTaskTrackerHttp());
			 * logURL.append("/tasklog?attemptid=");
			 * logURL.append(taskCompletionEvent.getTaskAttemptId().toString());
			 * logURL.append("&plaintext=true"); logURL.append("&filter=" +
			 * TaskLog.LogName.STDOUT);
			 * 
			 * log.info("Downloading " + logURL + "...");
			 * 
			 * try { URL url = new URL(logURL.toString()); HttpURLConnection
			 * conn = (HttpURLConnection) url .openConnection();
			 * BufferedInputStream in = new BufferedInputStream(
			 * conn.getInputStream());
			 * 
			 * String local = folder + "/" +
			 * taskCompletionEvent.getTaskAttemptId().toString() +
			 * "_stdout.txt";
			 * 
			 * BufferedOutputStream out = new BufferedOutputStream( new
			 * FileOutputStream(local)); IOUtils.copy(in, out);
			 * 
			 * IOUtils.closeQuietly(in); IOUtils.closeQuietly(out); } catch
			 * (Exception e) { log.error("Downloading log files failed.", e);
			 * return; }
			 * 
			 * logURL = new StringBuilder(
			 * taskCompletionEvent.getTaskTrackerHttp());
			 * logURL.append("/tasklog?attemptid=");
			 * logURL.append(taskCompletionEvent.getTaskAttemptId().toString());
			 * logURL.append("&plaintext=true"); logURL.append("&filter=" +
			 * TaskLog.LogName.STDERR);
			 * 
			 * log.info("Downloading " + logURL + "...");
			 * 
			 * try { URL url = new URL(logURL.toString()); HttpURLConnection
			 * conn = (HttpURLConnection) url .openConnection();
			 * BufferedInputStream in = new BufferedInputStream(
			 * conn.getInputStream());
			 * 
			 * String local = folder + "/" +
			 * taskCompletionEvent.getTaskAttemptId().toString() +
			 * "_stderr.txt";
			 * 
			 * BufferedOutputStream out = new BufferedOutputStream( new
			 * FileOutputStream(local)); IOUtils.copy(in, out);
			 * 
			 * IOUtils.closeQuietly(in); IOUtils.closeQuietly(out); } catch
			 * (Exception e) { log.error("Downloading log files failed.", e);
			 * return; }
			 * 
			 * }
			 */

		}

		log.info("Downloading log files successful.");

	}

}
