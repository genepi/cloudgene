package cloudgene.mapred.resources.jobs;

import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.representations.JSONAnswer;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.S3Util;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlParameter;
import cloudgene.mapred.wdl.WdlReader;

public class NewSubmitJob extends BaseResource {

	public static final int MAX_RUNNING_JOBS = 20;

	public static final int MAX_RUNNING_JOBS_PER_USER = 2;

	@Post
	public Representation post(Representation entity) {

		User user = getUser(getRequest());

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");

		}

		WorkflowEngine engine = getWorkflowEngine();

		if (engine.getActiveCount() > MAX_RUNNING_JOBS) {

			JSONObject answer = new JSONObject();
			try {
				answer.put("success", false);
				answer.put("message", "More than " + MAX_RUNNING_JOBS
						+ "  jobs are currently in the queue.");
			} catch (Exception e) {
				e.printStackTrace();
			}

			return new StringRepresentation(answer.toString());

		}

		if (engine.getJobsByUser(user).size() > MAX_RUNNING_JOBS_PER_USER) {

			JSONObject answer = new JSONObject();
			try {
				answer.put("success", false);
				answer.put("message", "Only " + MAX_RUNNING_JOBS_PER_USER
						+ " jobs per user can be executed simultaneously.");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return new StringRepresentation(answer.toString());

		}

		// tring tool = "minimac/new-simple";// props.get("tool");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
		String id = "job-" + sdf.format(new Date());// props.get("job-name");
		String hdfsWorkspace = HdfsUtil.path(getSettings().getHdfsWorkspace(),
				user.getUsername());
		String localWorkspace = FileUtil.path(
				getSettings().getLocalWorkspace(), user.getUsername());

		Map<String, String> props = new HashMap<String, String>();

		if (MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true)) {

			List<FileItem> items = parseRequest();

			// uploaded files
			for (FileItem item : items) {
				String name = item.getName();

				if (name != null) {

					// file parameter
					// write local file
					String tmpFile = getSettings().getTempFilename(
							item.getName());
					File file = new File(tmpFile);
					try {
						item.write(file);
					} catch (Exception e) {
						e.printStackTrace();
					}
					// import into hdfs
					if (file.exists()) {
						String entryName = item.getName();

						// remove upload indentification!
						String fieldName = item.getFieldName()
								.replace("-upload", "").replace("input-", "");

						String targetPath = HdfsUtil.path(hdfsWorkspace,
								"input", id, fieldName);

						String target = HdfsUtil.path(targetPath, entryName);

						HdfsUtil.put(tmpFile, target);

						// deletes temporary file
						FileUtil.deleteFile(tmpFile);

						if (props.containsKey(fieldName)) {
							// folder
							props.put(fieldName,
									HdfsUtil.path("input", id, fieldName));
						} else {
							// file
							props.put(fieldName, HdfsUtil.path("input", id,
									fieldName, entryName));
						}
					}

				}

			}

			// normal parameter
			for (FileItem item : items) {

				String name = item.getName();

				if (name == null) {

					try {
						if (item.getFieldName().startsWith("input-")) {
							String key = item.getFieldName().replace("input-",
									"");
							String value = new String(item.get(), "UTF-8");
							if (!props.containsKey(key)) {
								// don't override uploaded files
								props.put(key, value);
							}

						}

					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}

			}

			// checkboxes

		}

		// Form form = new Form(entity);

		// check aws credentials and s3 bucket
		if (user.isExportToS3()) {

			if (!S3Util.checkBucket(user.getAwsKey(), user.getAwsSecretKey(),
					user.getS3Bucket())) {

				return new JSONAnswer(
						"Your AWS-Credentials are wrong or your S3-Bucket doesn't exists.<br/><br/>Please update your AWS-Credentials.",
						false);
			}

		}

		String filename = getSettings().getApp(user);
		WdlApp app = null;
		try {
			app = WdlReader.loadAppFromFile(filename);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (app.getMapred() != null) {

			try {

				Map<String, String> params = new HashMap<>();

				for (WdlParameter input : app.getMapred().getInputs()) {
					if (props.containsKey(input.getId())) {
						if (input.getType().equals("checkbox")) {
							params.put(input.getId(),
									input.getValues().get("true"));
						} else {
							params.put(input.getId(), props.get(input.getId()));
						}
					} else {
						if (input.getType().equals("checkbox")) {
							params.put(input.getId(),
									input.getValues().get("false"));
						}
					}
				}

				CloudgeneJob job = new CloudgeneJob(user, id, app.getMapred(),
						params);
				job.setId(id);
				job.setName(id);
				job.setLocalWorkspace(localWorkspace);
				job.setHdfsWorkspace(hdfsWorkspace);
				job.setSettings(getSettings());
				job.setRemoveHdfsWorkspace(getSettings()
						.isRemoveHdfsWorkspace());
				engine.submit(job);

			} catch (Exception e) {
				e.printStackTrace();
				return new JSONAnswer(e.getMessage(), false);

			}

		}

		JSONObject answer = new JSONObject();
		try {
			answer.put("id", id);
			answer.put("success", true);
			answer.put("message",
					"Your job was successfully added to the job queue.");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new StringRepresentation(answer.toString());

	}

	private List<FileItem> parseRequest() {
		List<FileItem> items = null;
		// 1/ Create a factory for disk-based file items
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(1000240);

		// 2/ Create a new file upload handler based on the Restlet
		// FileUpload extension that will parse Restlet requests and
		// generates FileItems.
		RestletFileUpload upload = new RestletFileUpload(factory);

		try {
			items = upload.parseRequest(getRequest());
		} catch (FileUploadException e2) {
			e2.printStackTrace();
		}
		return items;
	}

}
