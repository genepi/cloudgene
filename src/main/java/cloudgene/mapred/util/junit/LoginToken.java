package cloudgene.mapred.util.junit;

import org.restlet.data.CookieSetting;

public class LoginToken {

		private CookieSetting cookie;

		private String csrfToken = "";

		public CookieSetting getCookie() {
			return cookie;
		}

		public void setCookie(CookieSetting cookie) {
			this.cookie = cookie;
		}

		public String getCsrfToken() {
			return csrfToken;
		}

		public void setCsrfToken(String csrfToken) {
			this.csrfToken = csrfToken;
		}

	}