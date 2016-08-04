package cloudgene.mapred.api.v2.users;

import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.representations.JSONAnswer;
import cloudgene.mapred.util.BaseResource;

public class ActivateUser extends BaseResource {

	@Get
	public Representation get() {

		String username = (String) getRequest().getAttributes().get("user");
		String code = (String) getRequest().getAttributes().get("code");

		UserDao dao = new UserDao(getDatabase());
		User user = dao.findByUsername(username);

		if (user != null) {

			if (user.getActivationCode() != null && user.getActivationCode().equals(code)) {

				user.setActive(true);
				user.setActivationCode("");
				dao.update(user);
				return new JSONAnswer("User sucessfully activated.", true);

			} else {

				return new JSONAnswer("Wrong activation code.", false);

			}
		} else {

			return new JSONAnswer("Wrong username.", false);

		}

	}

}
