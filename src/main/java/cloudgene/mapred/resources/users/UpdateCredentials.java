package cloudgene.mapred.resources.users;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.representations.JSONAnswer;

public class UpdateCredentials extends ServerResource {

	@Post
	public Representation post(Representation entity) {

		UserSessions sessions = UserSessions.getInstance();
		User user = sessions.getUserByRequest(getRequest());
		if (user != null) {

			Form form = new Form(entity);

			String awsKey = form.getFirstValue("aws-key");
			String awsSecretKey = form.getFirstValue("aws-secret-key");
			String saveKeys = form.getFirstValue("save-keys");

			user.setAwsKey(awsKey);
			user.setAwsSecretKey(awsSecretKey);

			if (saveKeys != null && saveKeys.equals("on")) {

				user.setSaveCredentials(true);

				UserDao dao = new UserDao();
				dao.update(user);

			} else {

				user.setSaveCredentials(false);

				UserDao dao = new UserDao();
				dao.update(user);

			}

			return new JSONAnswer("OK.", true);

		} else {

			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return new JSONAnswer("Please log in.", false);

		}
	}

}
