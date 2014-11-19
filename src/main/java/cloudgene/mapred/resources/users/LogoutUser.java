package cloudgene.mapred.resources.users;

import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.util.BaseResource;

public class LogoutUser extends BaseResource {

	@Get
	public Representation get() {
		StringRepresentation representation = null;

		String token = getRequest().getCookies().getFirstValue(
				UserSessions.COOKIE_NAME);

		// logout and remove cookie
		if (token != null) {
			UserSessions sessions = getUserSessions();
			sessions.logoutUserByToken(token);
			getRequest().getCookies().removeAll(UserSessions.COOKIE_NAME);
		}

		getResponse().redirectTemporary("/");

		return representation;
	}

}
