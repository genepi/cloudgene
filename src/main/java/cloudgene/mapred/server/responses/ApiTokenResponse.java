package cloudgene.mapred.server.responses;

import cloudgene.mapred.core.ApiToken;
import com.fasterxml.jackson.annotation.JsonClassDescription;


@JsonClassDescription
public class ApiTokenResponse {

	private String token = "";

	private boolean success = true;

	private String message = "";

	private int type;

	private long time;

	public ApiTokenResponse(ApiToken token) {
		this("Creation successfull.", true);
		this.token = token.getAccessToken();
	}

	public ApiTokenResponse(String message, boolean success) {
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

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

}
