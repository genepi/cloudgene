package cloudgene.mapred.jobs.cache;

import cloudgene.mapred.core.User;

public class CacheEntry {

	private int id;

	private String signature;

	private int used;

	private long lastUsedOn = 0;

	private long createdOn = 0;

	private long size = 0;

	private User user;

	private long executionTime = 0;

	private String output;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public int getUsed() {
		return used;
	}

	public void setUsed(int used) {
		this.used = used;
	}

	public long getLastUsedOn() {
		return lastUsedOn;
	}

	public void setLastUsedOn(long lastUseOn) {
		this.lastUsedOn = lastUseOn;
	}

	public long getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(long createdOn) {
		this.createdOn = createdOn;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public long getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(long executionTime) {
		this.executionTime = executionTime;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

}
