package cloudgene.mapred.util;

import org.restlet.data.CookieSetting;

public class LoginToken {

	private CookieSetting cookie;

	private String accessToken = "";

	private String csrfToken = "";

	public CookieSetting getCookie() {
		return cookie;
	}

	public void setCookie(CookieSetting cookie) {
		this.cookie = cookie;
	}

	public String getAccessToken() {
		return accessToken;
	}
	
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getCsrfToken() {
		return csrfToken;
	}

	public void setCsrfToken(String csrfToken) {
		this.csrfToken = csrfToken;
	}

}