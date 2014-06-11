package cloudgene.mapred.resources.users;

import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.representations.JSONAnswer;

public class ActivateUser extends ServerResource {

	@Get
	public Representation get() {

		String username = (String) getRequest().getAttributes().get("user");
		String code = (String) getRequest().getAttributes().get("code");

		UserDao dao = new UserDao();
		User user = dao.findByUsername(username);

		if (user != null) {

			if (user.getActivationCode().equals(code)) {
				user.setActive(true);
				dao.update(user);
				return new JSONAnswer("User sucessfully created.", true);
			} else {
				return new JSONAnswer("Wrong activation code.", false);
			}
		} else {
			return new JSONAnswer("Wrong username.", false);
		}

	}

}
