package cloudgene.mapred.server.responses;

import java.io.File;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import org.apache.commons.io.FileUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import cloudgene.mapred.jobs.AbstractJob;
import genepi.io.FileUtil;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonClassDescription
public class JobAdminResponse {

	private List<JobResponse> data;
	private int count;
	private int success;
	private int failed;
	private int pending;
	private int waiting;
	private int running;
	private int canceld;

	public int getSuccess() {
		return success;
	}

	public void setSuccess(int success) {
		this.success = success;
	}

	public int getFailed() {
		return failed;
	}

	public void setFailed(int failed) {
		this.failed = failed;
	}

	public int getPending() {
		return pending;
	}

	public void setPending(int pending) {
		this.pending = pending;
	}

	public int getWaiting() {
		return waiting;
	}

	public void setWaiting(int waiting) {
		this.waiting = waiting;
	}

	public int getRunning() {
		return running;
	}

	public void setRunning(int running) {
		this.running = running;
	}

	public int getCanceld() {
		return canceld;
	}

	public void setCanceld(int canceld) {
		this.canceld = canceld;
	}
	
	public static JobAdminResponse build(List<AbstractJob> jobs, List<JobResponse> responses, String workspace) {
		
		int success = 0;
		int failed = 0;
		int pending = 0;
		int waiting = 0;
		int canceld = 0;
		int running = 0;
		
		JobAdminResponse response = new JobAdminResponse();
		response.setData(responses);
		response.setCount(jobs.size());
		
		for (AbstractJob job : jobs) {

			String folder = FileUtil.path(workspace, job.getId());
			File file = new File(folder);
			if (file.exists()) {
				long size = FileUtils.sizeOfDirectory(file);
				job.setWorkspaceSize(FileUtils.byteCountToDisplaySize(size));
			}

			if (job.getState() == AbstractJob.STATE_EXPORTING || job.getState() == AbstractJob.STATE_RUNNING) {
				running++;
			}
			if (job.getState() == AbstractJob.STATE_SUCCESS
					|| job.getState() == AbstractJob.STATE_SUCESS_AND_NOTIFICATION_SEND) {
				success++;
			}
			if (job.getState() == AbstractJob.STATE_FAILED
					|| job.getState() == AbstractJob.STATE_FAILED_AND_NOTIFICATION_SEND) {
				failed++;
			}
			if (job.getState() == AbstractJob.STATE_DEAD) {
				pending++;
			}
			if (job.getState() == AbstractJob.STATE_WAITING) {
				waiting++;
			}
			if (job.getState() == AbstractJob.STATE_CANCELED) {
				canceld++;
			}
		}
		
		response.setSuccess(success);
		response.setFailed(failed);
		response.setPending(pending);
		response.setWaiting(waiting);
		response.setCanceld(canceld);
		response.setRunning(running);
		
		return response;

	}

	public List<JobResponse> getData() {
		return data;
	}

	public void setData(List<JobResponse> data) {
		this.data = data;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}


}
