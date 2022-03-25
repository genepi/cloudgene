package cloudgene.mapred.core;

public class ApiToken {

	private String accessToken;
	
	private String hash;

	public ApiToken(String accessToken, String hash) {
		this.accessToken = accessToken;
		this.hash = hash;
	}
	
	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}
	
	
	
}
