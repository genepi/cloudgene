package cloudgene.mapred.resources.users;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.representations.JSONAnswer;
import cloudgene.mapred.util.BaseResource;

public class UpdateCredentials extends BaseResource {

	@Post
	public Representation post(Representation entity) {

		User user = getUser(getRequest());

		if (user != null) {

			Form form = new Form(entity);

			String awsKey = form.getFirstValue("aws-key");
			String awsSecretKey = form.getFirstValue("aws-secret-key");
			String saveKeys = form.getFirstValue("save-keys");

			user.setAwsKey(awsKey);
			user.setAwsSecretKey(awsSecretKey);

			if (saveKeys != null && saveKeys.equals("on")) {

				user.setSaveCredentials(true);

				UserDao dao = new UserDao(getDatabase());
				dao.update(user);

			} else {

				user.setSaveCredentials(false);

				UserDao dao = new UserDao(getDatabase());
				dao.update(user);

			}

			return new JSONAnswer("OK.", true);

		} else {

			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return new JSONAnswer("Please log in.", false);

		}
	}

}
