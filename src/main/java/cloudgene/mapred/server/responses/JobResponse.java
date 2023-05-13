package cloudgene.mapred.server.responses;

import java.util.List;
import java.util.Vector;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneParameterOutput;
import cloudgene.mapred.util.JobResultsTreeUtil;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class JobResponse {

	private String application;
	private String applicationId;
	private boolean canceld;
	private boolean complete;
	private long deletedOn;
	private long endTime;
	private long finishedOn;
	private String id;
	private String name;
	private String logs = "";
	private int state;
	private int positionInQueue;
	private String userAgent;
	private int progress;
	private long startTime;
	private long setupStartTime;
	private long setupEndTime;
	private long submittedOn;
	private boolean setupComplete;
	private boolean setupRunning;
	private String username;

	@JsonProperty("steps")
	private List<StepResponse> stepResponses;

	@JsonProperty("outputParams")
	private List<ParameterOutputResponse> parameterOutputResponse;

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public boolean isCanceld() {
		return canceld;
	}

	public void setCanceld(boolean canceld) {
		this.canceld = canceld;
	}

	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	public long getDeletedOn() {
		return deletedOn;
	}

	public void setDeletedOn(long deletedOn) {
		this.deletedOn = deletedOn;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public long getFinishedOn() {
		return finishedOn;
	}

	public void setFinishedOn(long finishedOn) {
		this.finishedOn = finishedOn;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<StepResponse> getStepResponses() {
		return stepResponses;
	}

	public void setStepResponses(List<StepResponse> stepResponses) {
		this.stepResponses = stepResponses;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getPositionInQueue() {
		return positionInQueue;
	}

	public void setPositionInQueue(int positionInQueue) {
		this.positionInQueue = positionInQueue;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getSetupStartTime() {
		return setupStartTime;
	}

	public void setSetupStartTime(long setupStartTime) {
		this.setupStartTime = setupStartTime;
	}

	public long getSetupEndTime() {
		return setupEndTime;
	}

	public void setSetupEndTime(long setupEndTime) {
		this.setupEndTime = setupEndTime;
	}

	public long getSubmittedOn() {
		return submittedOn;
	}

	public void setSubmittedOn(long submittedOn) {
		this.submittedOn = submittedOn;
	}

	public boolean isSetupComplete() {
		return setupComplete;
	}

	public void setSetupComplete(boolean setupComplete) {
		this.setupComplete = setupComplete;
	}

	public boolean isSetupRunning() {
		return setupRunning;
	}

	public void setSetupRunning(boolean setupRunning) {
		this.setupRunning = setupRunning;
	}

	public List<ParameterOutputResponse> getParameterOutputResponse() {
		return parameterOutputResponse;
	}

	public void setParameterOutputResponse(List<ParameterOutputResponse> parameterOutputResponse) {
		this.parameterOutputResponse = parameterOutputResponse;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getLogs() {
		return logs;
	}

	public void setLogs(String logs) {
		this.logs = logs;
	}

	public static JobResponse build(AbstractJob job, User user) {

		// create tree
		for (CloudgeneParameterOutput param : job.getOutputParams()) {
			String hash = param.createHash();
			param.setHash(hash);
			param.setTree(JobResultsTreeUtil.createTree(param.getFiles()));
		}

		// removes outputs that are for admin only
		List<CloudgeneParameterOutput> adminParams = new Vector<>();
		if (!user.isAdmin()) {
			for (CloudgeneParameterOutput param : job.getOutputParams()) {
				if (param.isAdminOnly()) {
					adminParams.add(param);
				}
			}
		}

		// remove all outputs that are not downloadable
		for (CloudgeneParameterOutput param : job.getOutputParams()) {
			if (!param.isDownload()) {
				adminParams.add(param);
			}
		}
		job.getOutputParams().removeAll(adminParams);

		JobResponse response = new JobResponse();
		response.setApplication(job.getApplication());
		response.setApplicationId(job.getApplicationId());
		response.setCanceld(job.isCanceld());
		response.setComplete(job.isComplete());
		response.setName(job.getName());
		response.setId(job.getId());

		response.setState(job.getState());
		response.setLogs(job.getLogs());

		response.setPositionInQueue(job.getPositionInQueue());
		response.setProgress(job.getProgress());
		response.setUserAgent(job.getUserAgent());

		response.setSetupComplete(job.isSetupComplete());
		response.setSetupRunning(job.isRunning());
		response.setDeletedOn(job.getDeletedOn());

		response.setStartTime(job.getStartTime());
		response.setEndTime(job.getEndTime());
		response.setFinishedOn(job.getFinishedOn());
		response.setSubmittedOn(job.getSubmittedOn());
		response.setSetupStartTime(job.getSetupStartTime());
		response.setSetupEndTime(job.getSetupEndTime());
		List<StepResponse> responses = StepResponse.build(job.getSteps());
		response.setStepResponses(responses);

		List<ParameterOutputResponse> responsesParamsOut = ParameterOutputResponse.build(job.getOutputParams());
		response.setParameterOutputResponse(responsesParamsOut);

		// set log if user is admin
		if (user.isAdmin()) {
			// job.setLogs("logs/" + job.getId());
			response.setLogs("logs/" + job.getId());
		} else {
			response.setLogs("");
		}

		response.setUsername(user.getUsername());

		return response;
	}

	public static List<JobResponse> build(List<AbstractJob> data, User user) {
		List<JobResponse> responses = new Vector<JobResponse>();
		for (AbstractJob job : data) {
			responses.add(JobResponse.build(job, user));
		}
		return responses;
	}

}
