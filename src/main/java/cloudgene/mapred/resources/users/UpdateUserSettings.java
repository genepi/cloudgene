package cloudgene.mapred.resources.users;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.representations.JSONAnswer;

public class UpdateUserSettings extends ServerResource {

	@Post
	public Representation post(Representation entity) {

		UserSessions sessions = UserSessions.getInstance();
		User user = sessions.getUserByRequest(getRequest());
		if (user != null) {
			try {
				JsonRepresentation represent = new JsonRepresentation(entity);
				JSONObject obj = represent.getJsonObject();

				// AWS Credentials
				String saveKeys = null;
				String awsKey = null;
				String awsSecretKey = null;

				if (obj.has("save-keys")) {
					awsKey = obj.get("aws-key").toString();
					awsSecretKey = obj.get("aws-secret-key").toString();
					saveKeys = obj.get("save-keys").toString();
				}

				if (saveKeys != null && saveKeys.equals("on")) {

					user.setAwsSecretKey(awsSecretKey);
					user.setAwsKey(awsKey);
					user.setSaveCredentials(true);

				} else {

					user.setAwsSecretKey("");
					user.setAwsKey("");
					user.setSaveCredentials(false);

				}

				// Export to s3 bucket

				String exortToS3 = null;
				String s3Bucket = null;
				String exportInputToS3 = "off";

				if (obj.has("export-to-s3")) {
					exortToS3 = obj.get("export-to-s3").toString();
					s3Bucket = obj.get("s3-bucket").toString();
					if (obj.has("export-input-to-s3")) {
						exportInputToS3 = obj.get("export-input-to-s3")
								.toString();
					}
				}

				if (exortToS3 != null && exortToS3.equals("on")) {

					user.setExportToS3(true);
					user.setS3Bucket(s3Bucket);
					user.setExportInputToS3(exportInputToS3.equals("on"));

				} else {

					user.setExportInputToS3(false);
					user.setExportToS3(false);
					user.setS3Bucket("");

				}

				// General Informations
				user.setFullName(obj.get("full-name").toString());
				user.setMail(obj.get("mail").toString());

				UserDao dao = new UserDao();
				dao.update(user);

				return new JSONAnswer("Password sucessfully updated.", true);

			} catch (JSONException e) {

				getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				e.printStackTrace();
				return new JSONAnswer("Please log in.", false);

			} catch (IOException e) {

				getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);

				e.printStackTrace();
				return new JSONAnswer("Please log in.", false);
			}

		} else {

			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return new JSONAnswer("Please log in.", false);

		}

	}
}
