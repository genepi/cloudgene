package cloudgene.mapred.server.responses;

public class ResponseObject {

	private boolean success;
	private String message;
	private String id;

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public static ResponseObject build(String id, String message, boolean success) {
		ResponseObject response = new ResponseObject();
		response.setId(id);
		response.setMessage(message);
		response.setSuccess(success);
		return response;
	}

}
