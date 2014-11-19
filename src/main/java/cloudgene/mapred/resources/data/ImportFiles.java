package cloudgene.mapred.resources.data;

import genepi.io.FileUtil;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.TaskJob;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.tasks.AbstractTask;
import cloudgene.mapred.tasks.ImporterFtp;
import cloudgene.mapred.tasks.ImporterHttp;
import cloudgene.mapred.tasks.ImporterLocalFile;
import cloudgene.mapred.tasks.ImporterS3;
import cloudgene.mapred.tasks.ImporterSftp;
import cloudgene.mapred.util.BaseResource;

public class ImportFiles extends BaseResource {

	@Post
	public Representation post(Representation entity) {

		StringRepresentation representation = null;

		try {
			JsonRepresentation represent = new JsonRepresentation(entity);

			User user = getUser(getRequest());

			JSONObject obj = represent.getJsonObject();

			if (user != null) {

				String server = obj.get("server").toString();

				String username = "";
				String password = "";
				int port = 0;

				if (obj.has("username")) {
					username = obj.get("username").toString();
				}
				if (obj.has("password")) {
					password = obj.get("password").toString();
				}
				if (obj.has("port")) {
					port = Integer.parseInt(obj.get("port").toString());
				}
				String path = obj.get("path").toString();

				// Importer Task

				AbstractTask task = null;

				if (server.startsWith("http://")) {

					String url = server;
					task = new ImporterHttp(url, path);

				} else if (server.startsWith("s3n://")) {

					String type = obj.get("type").toString();

					String bucket = server;

					String key = null;
					String secret = null;
					if (type.equals("private")) {
						key = user.getAwsKey();
						secret = user.getAwsSecretKey();
					}
					task = new ImporterS3(bucket, key, secret, path);

				} else if (server.startsWith("ftp://")) {

					task = new ImporterFtp(server, username, password, path);

				} else if (server.startsWith("sftp://")) {

					task = new ImporterSftp(server, username, password, path,
							port);

				} else {
					String filename = FileUtil.path(getSettings()
							.getLocalWorkspace(), user.getUsername(), server);
					task = new ImporterLocalFile(filename, path, false);
				}

				// Submit Job
				AbstractJob job = new TaskJob(task);
				job.setName(job.getId());
				job.setUser(user);

				WorkflowEngine engine = getWorkflowEngine();
				engine.submit(job);

				// Response

				representation = new StringRepresentation(
						"File import started!");
				getResponse().setStatus(Status.SUCCESS_OK);
				getResponse().setEntity(representation);
				return representation;

			} else {
				representation = new StringRepresentation("No user");
				getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				getResponse().setEntity(representation);
				return representation;

			}
		} catch (JSONException e) {
			representation = new StringRepresentation(e.getMessage());
			getResponse().setEntity(representation);
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			e.printStackTrace();
			return representation;
		} catch (IOException e) {
			representation = new StringRepresentation(e.getMessage());
			getResponse().setEntity(representation);
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			e.printStackTrace();
			return representation;
		}

	}

	/**
	 * Mandatory. Specifies that this resource supports POST requests.
	 */
	public boolean allowPost() {
		return true;
	}

}
