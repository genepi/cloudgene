package cloudgene.mapred.server.responses;

import java.util.List;
import java.util.Vector;

import cloudgene.mapred.jobs.Message;

public class MessageResponse {

	private boolean success = true;

	private String message = "";

	private int type;

	private long time;

	protected MessageResponse() {
	}

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

	public static MessageResponse success(String message) {
		return new MessageResponse(message, true);
	}

	public static MessageResponse error(String message) {
		return new MessageResponse(message, false);
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public static MessageResponse build(Message message) {
		MessageResponse response = new MessageResponse();
		response.setMessage(message.getMessage());
		response.setTime(message.getTime());
		response.setType(message.getType());
		return response;
	}

	public static List<MessageResponse> build(List<Message> messages) {
		List<MessageResponse> response = new Vector<MessageResponse>();
		for (Message message : messages) {
			response.add(MessageResponse.build(message));
		}
		return response;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
