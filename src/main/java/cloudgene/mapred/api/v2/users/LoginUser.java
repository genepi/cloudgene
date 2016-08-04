package cloudgene.mapred.api.v2.users;

import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.JWTUtil;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.representations.JSONAnswer;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.HashUtil;

public class LoginUser extends BaseResource {

	@Post
	public Representation post(Representation entity) {
		Form form = new Form(entity);

		String username = form.getFirstValue("loginUsername");
		String password = form.getFirstValue("loginPassword");
		password = HashUtil.getMD5(password);

		UserDao dao = new UserDao(getDatabase());
		User user = dao.findByUsername(username);

		if (user != null) {
			if (user.getPassword().equals(password) && user.isActive()) {

				// create session
				String token = JWTUtil.createToken(user,getSettings().getSecretKey());
				// set cookie
				CookieSetting cookie = new CookieSetting(
						JWTUtil.COOKIE_NAME, token);
				getResponse().getCookieSettings().add(cookie);

				return new JSONAnswer("Login successfull.", true);

			} else {
				return new JSONAnswer(
						"Login Failed! Wrong Username or Password.", false);
			}
		} else {
			return new JSONAnswer("Login Failed! Wrong Username or Password.",
					false);
		}
	}

}
