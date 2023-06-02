package cloudgene.mapred.core;

import java.util.Date;

public class ApiToken {

	private String accessToken;

	private String hash;

	private Date expiresOn;

	public ApiToken(String accessToken, String hash, Date expiresOn) {
		this.accessToken = accessToken;
		this.hash = hash;
		this.expiresOn = expiresOn;
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

	public void setExpiresOn(Date expiresOn) {
		this.expiresOn = expiresOn;
	}

	public Date getExpiresOn() {
		return expiresOn;
	}

}