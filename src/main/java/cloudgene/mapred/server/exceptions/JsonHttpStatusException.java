package cloudgene.mapred.server.exceptions;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;

public class JsonHttpStatusException extends HttpStatusException {

	private static final long serialVersionUID = 1L;

	private MessageWrapper object;

	public JsonHttpStatusException(HttpStatus status, String message) {
		super(status, new MessageWrapper(message, false));
		object = new MessageWrapper(message, false);
	}

	public MessageWrapper getObject() {
		return object;
	}
}
