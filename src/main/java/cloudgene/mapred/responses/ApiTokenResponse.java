package cloudgene.mapred.responses;

import cloudgene.mapred.core.ApiToken;

public class ApiTokenResponse extends MessageResponse {

	private String token = "";

	public ApiTokenResponse(ApiToken token) {
		super("Creation successfull.", true);
		this.token = token.getAccessToken();
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

}
