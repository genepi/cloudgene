package cloudgene.mapred.util;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.routing.Filter;

import cloudgene.mapred.WebApp;
import cloudgene.mapred.core.User;
import genepi.db.Database;
import cloudgene.mapred.core.JWTUtil;

public class LoginFilter extends Filter {

	private String loginPage;

	private String prefix = "";

	private String secretKey = "";

	public LoginFilter(String loginPage, String prefix, String secretKey) {
		this.loginPage = loginPage;
		this.prefix = prefix;
		this.secretKey = secretKey;
	}

	@Override
	protected int beforeHandle(Request request, Response response) {

		WebApp application = (WebApp) getApplication();
		Database database = application.getDatabase();

		String path = request.getResourceRef().getPath();

		if (path.toLowerCase().equals(prefix + loginPage)) {
			User user = JWTUtil.getUserByRequest(database, request, secretKey, false);
			if (user != null) {
				response.redirectTemporary(prefix + "/start.html");
				return STOP;
			}

		}
		return CONTINUE;
	}

}
