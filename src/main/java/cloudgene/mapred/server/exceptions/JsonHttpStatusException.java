package cloudgene.mapred.server.exceptions;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;

public class JsonHttpStatusException extends HttpStatusException {

	private static final long serialVersionUID = 1L;

	public JsonHttpStatusException(HttpStatus status, String message) {
		super(status, new MessageWrapper(message, false));
	}

	public static class MessageWrapper {

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

}
