package cloudgene.mapred.server.responses;

public class ValidatedApiTokenResponse {

	private boolean valid = false;
	
	private String message;

	protected ValidatedApiTokenResponse(String message, boolean valid) {
		this.message = message;
		this.valid = valid;
	}
	
	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public static ValidatedApiTokenResponse valid(String message) {
		return new ValidatedApiTokenResponse(message, true);
	}
	
	
	public static ValidatedApiTokenResponse error(String message) {
		return new ValidatedApiTokenResponse(message, false);
	}
}
