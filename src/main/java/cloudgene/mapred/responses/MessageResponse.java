package cloudgene.mapred.responses;

public class MessageResponse {

	private boolean success = true;

	private String message = "";

	private String type = "plain";

	protected MessageResponse(String message, boolean success) {
		this.message = message;
		this.success = success;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public static MessageResponse success(String message) {
		return new MessageResponse(message, true);
	}

	public static MessageResponse error(String message) {
		return new MessageResponse(message, false);
	}
}
