package cloudgene.mapred.resources.users;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.JWTUtil;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.representations.JSONAnswer;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.HashUtil;

public class AuthUserAPI extends BaseResource {

	@Post
	public Representation post(Representation entity) {
		Form form = new Form(entity);

		String username = form.getFirstValue("username");
		String password = form.getFirstValue("password");
		password = HashUtil.getMD5(password);

		if (username == null || password == null){
			return error(Status.CLIENT_ERROR_BAD_REQUEST, "Wrong parameters.");
		}
		
		UserDao dao = new UserDao(getDatabase());
		User user = dao.findByUsername(username);

		if (user != null) {
			if (user.getPassword().equals(password) && user.isActive()) {

				// create token
				String token = JWTUtil.createToken(user);

				// return token

				JSONObject answer = new JSONObject();
				try {
					answer.put("success", true);
					answer.put("message", "Login successfull.");
					answer.put("token", token);
					answer.put("type", "plain");
					return new StringRepresentation(answer.toString());
				} catch (JSONException e) {
					return error401("Login Failed! Wrong Username or Password.");
				}

			} else {
				return error401("Login Failed! Wrong Username or Password.");
			}
		} else {
			return error401("Login Failed! Wrong Username or Password.");
		}
	}

}
