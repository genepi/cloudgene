package cloudgene.mapred.resources.jobs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.jobs.CloudgeneParameter;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.representations.JSONAnswer;
import cloudgene.mapred.util.S3Util;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlReader;

public class RerunJob extends ServerResource {

	@Post
	public Representation post(Representation entity) {

		UserSessions sessions = UserSessions.getInstance();
		User user = sessions.getUserByRequest(getRequest());
		
		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");

		}

		// check aws credentials and s3 bucket
		if (user.isExportToS3()) {

			if (!S3Util.checkBucket(user.getAwsKey(), user.getAwsSecretKey(),
					user.getS3Bucket())) {

				return new JSONAnswer(
						"Your AWS-Credentials are wrong or your S3-Bucket doesn't exists.<br/><br/>Please update your AWS-Credentials.",
						false);

			}

		}

		WorkflowEngine queue = WorkflowEngine.getInstance();

		Form form = new Form(entity);
		String jobId = form.getFirstValue("id");
		if (jobId != null) {

			JobDao dao = new JobDao();
			AbstractJob oldJob = dao.findById(jobId);

			String[] tiles = jobId.split("-");

			String tool = tiles[0];
			for (int i = 1; i < tiles.length - 2; i++) {
				tool += "-" + tiles[i];
			}

			WdlApp app = WdlReader.loadApp(tool);
			if (app.getMapred() != null) {

				try {

					Map<String, String> newParams = new HashMap<String, String>();
					for (CloudgeneParameter params : oldJob.getInputParams()) {
						String key = params.getName();
						String value = params.getValue();
						newParams.put(key, value);

					}

					CloudgeneJob job = new CloudgeneJob(user, jobId,
							app.getMapred(), newParams);
					SimpleDateFormat sdf = new SimpleDateFormat(
							"yyyyMMdd-HHmmss");
					String name = tool + "-" + sdf.format(new Date());
					job.setId(name);
					job.setName(name);
					queue.submit(job);

				} catch (Exception e) {

					return new JSONAnswer(e.getMessage(), false);

				}

			}

			return new JSONAnswer(
					"Your job was successfully added to the job queue.", true);
		} else {

			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation("Job " + jobId + " not found.");

		}

	}

}
