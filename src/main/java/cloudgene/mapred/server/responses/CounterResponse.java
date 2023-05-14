package cloudgene.mapred.server.responses;

import java.util.HashMap;
import java.util.Map;

import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.WorkflowEngine;

public class CounterResponse {

	private Map<String, Long> complete = new HashMap<String, Long>();
	
	private Map<String, Long> running = new HashMap<String, Long>();
	
	private Map<String, Long> waiting = new HashMap<String, Long>();

	private int users = 0;

	public Map<String, Long> getComplete() {
		return complete;
	}

	public void setComplete(Map<String, Long> complete) {
		this.complete = complete;
	}

	public Map<String, Long> getRunning() {
		return running;
	}

	public void setRunning(Map<String, Long> running) {
		this.running = running;
	}

	public Map<String, Long> getWaiting() {
		return waiting;
	}

	public void setWaiting(Map<String, Long> waiting) {
		this.waiting = waiting;
	}

	public int getUsers() {
		return users;
	}

	public void setUsers(int users) {
		this.users = users;
	}
	
	public static CounterResponse build(WorkflowEngine workflowEngine) {
		CounterResponse response = new CounterResponse();
		response.complete = workflowEngine.getCounters(AbstractJob.STATE_SUCCESS);
		response.running = workflowEngine.getCounters(AbstractJob.STATE_RUNNING);
		response.waiting = workflowEngine.getCounters(AbstractJob.STATE_WAITING);
		return response;
	}
	
}
