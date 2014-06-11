package cloudgene.mapred.resources.jobs;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.representations.JSONAnswer;
import cloudgene.mapred.util.S3Util;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlReader;

public class SubmitJob extends ServerResource {

	@Post
	public Representation post(Representation entity) {
		UserSessions sessions = UserSessions.getInstance();
		User user = sessions.getUserByRequest(getRequest());
		if (user != null) {
			try {

				JsonRepresentation represent = new JsonRepresentation(entity);
				JSONObject obj = represent.getJsonObject();

				// check aws credentials and s3 bucket
				if (user.isExportToS3()) {

					if (!S3Util.checkBucket(user.getAwsKey(),
							user.getAwsSecretKey(), user.getS3Bucket())) {

						return new JSONAnswer(
								"Your AWS-Credentials are wrong or your S3-Bucket doesn't exists.<br/><br/>Please update your AWS-Credentials.",
								false);
					}

				}

				WorkflowEngine queue = WorkflowEngine.getInstance();

				String tool = obj.get("tool").toString();

				WdlApp app = WdlReader.loadApp(tool);
				if (app.getMapred() != null) {

					try {

						CloudgeneJob job = new CloudgeneJob(app.getMapred());

						String name = obj.get("job-name").toString();
						String id = obj.get("job-id").toString();
						job.setId(id);
						job.setName(name);
						job.setUser(user);

						String[] names = JSONObject.getNames(obj);

						// parse params
						for (String paramName : names) {
							System.out.println(paramName);
							if (paramName.startsWith("input-")) {
								String key = paramName.replace("input-", "");
								String value = obj.get(paramName).toString();
								job.setInputParam(key, value);
							}

						}

						queue.submit(job);

					} catch (Exception e) {
						e.printStackTrace();
						return new JSONAnswer(e.getMessage(), false);

					}

				}

				return new JSONAnswer(
						"Your job was successfully added to the job queue.",
						true);
			} catch (JSONException e) {
				getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				e.printStackTrace();
			} catch (IOException e) {
				getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				e.printStackTrace();
			}
		} else {
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);

		}

		getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		return new StringRepresentation("");

	}

}
