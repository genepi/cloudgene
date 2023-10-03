package cloudgene.mapred.api.v2.users;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.representations.JSONAnswer;
import cloudgene.mapred.util.BaseResource;

public class ActivateUser extends BaseResource {
	private static final Log log = LogFactory.getLog(ActivateUser.class);

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

				log.info(String.format("User: activated user %s (ID %s - email %s)", user.getUsername(), user.getId(), user.getMail()));

				return new JSONAnswer("User sucessfully activated.", true);

			} else {
				log.warn(String.format("User: code is either incorrect or has already been used for user %s (ID %s - email %s)", user.getUsername(), user.getId(), user.getMail()));
				return new JSONAnswer("Wrong activation code.", false);

			}
		} else {
			log.warn(String.format("User: used activation code for missing or unknown username '%s'", username));
			return new JSONAnswer("Wrong username.", false);

		}

	}

}
