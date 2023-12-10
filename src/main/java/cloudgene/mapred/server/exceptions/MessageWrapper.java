package cloudgene.mapred.server.exceptions;

import com.fasterxml.jackson.annotation.JsonClassDescription;

@JsonClassDescription
public class MessageWrapper {

	private String message;

	private boolean success = false;

	public MessageWrapper(String message, boolean success) {
		this.message = message;
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public boolean isSuccess() {
		return success;
	}
}