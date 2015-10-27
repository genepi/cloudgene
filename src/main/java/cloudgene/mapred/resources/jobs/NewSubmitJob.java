package cloudgene.mapred.resources.jobs;

import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
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
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlParameter;
import cloudgene.mapred.wdl.WdlReader;

public class NewSubmitJob extends BaseResource {

	@Post
	public Representation post(Representation entity) {

		User user = getUser(getRequest());

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");

		}

		WorkflowEngine engine = getWorkflowEngine();

		Settings settings = getSettings();

		if (engine.getActiveCount() >= settings.getMaxRunningJobs()) {

			JSONObject answer = new JSONObject();
			try {
				answer.put("success", false);
				answer.put("message",
						"More than " + settings.getMaxRunningJobs()
								+ "  jobs are currently in the queue.");
			} catch (Exception e) {
				e.printStackTrace();
			}

			return new StringRepresentation(answer.toString());

		}

		if (engine.getJobsByUser(user).size() >= settings
				.getMaxRunningJobsPerUser()) {

			JSONObject answer = new JSONObject();
			try {
				answer.put("success", false);
				answer.put(
						"message",
						"Only "
								+ settings.getMaxRunningJobsPerUser()
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

		String tool = getAttribute("tool");

		String filename = getSettings().getApp(user, tool);
		WdlApp app = null;
		try {
			app = WdlReader.loadAppFromFile(filename);
		} catch (Exception e1) {

			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation("Tool '" + tool + "' not found.");

		}

		try {
			FileItemIterator iterator = parseRequest(entity);

			// uploaded files
			while (iterator.hasNext()) {

				FileItemStream item = iterator.next();

				String name = item.getName();

				if (name != null) {

					// file parameter
					// write local file
					String tmpFile = getSettings().getTempFilename(
							item.getName());
					File file = new File(tmpFile);
					try {
						FileUtils
								.copyInputStreamToFile(item.openStream(), file);
					} catch (Exception e) {
						e.printStackTrace();
					}

					// import into hdfs
					if (file.exists()) {
						String entryName = item.getName();

						// remove upload indentification!
						String fieldName = item.getFieldName()
								.replace("-upload", "").replace("input-", "");

						boolean hdfs = false;
						boolean folder = false;

						for (WdlParameter input : app.getMapred().getInputs()) {
							if (input.getId().equals(fieldName)) {
								hdfs = (input.getType().equals(
										WdlParameter.HDFS_FOLDER) || input
										.getType().equals(
												WdlParameter.HDFS_FILE));
								folder = (input.getType()
										.equals(WdlParameter.HDFS_FOLDER))
										|| (input.getType()
												.equals(WdlParameter.LOCAL_FOLDER));
							}
						}

						if (hdfs) {

							String targetPath = HdfsUtil.path(hdfsWorkspace,
									"input", id, fieldName);

							String target = HdfsUtil
									.path(targetPath, entryName);

							HdfsUtil.put(tmpFile, target);

							// deletes temporary file
							FileUtil.deleteFile(tmpFile);

							if (folder) {
								// folder
								props.put(fieldName,
										HdfsUtil.path("input", id, fieldName));
							} else {
								// file
								props.put(fieldName, HdfsUtil.path("input", id,
										fieldName, entryName));
							}

						} else {

							String targetPath = FileUtil.path(localWorkspace,
									"input", id, fieldName);

							FileUtil.createDirectory(FileUtil.path(
									localWorkspace, "input"));

							FileUtil.createDirectory(FileUtil.path(
									localWorkspace, "input", id));

							FileUtil.createDirectory(FileUtil.path(
									localWorkspace, "input", id, fieldName));

							String target = FileUtil
									.path(targetPath, entryName);

							FileUtil.copy(tmpFile, target);

							// deletes temporary file
							FileUtil.deleteFile(tmpFile);

							if (folder) {
								// folder
								props.put(
										fieldName,
										new File(FileUtil.path(localWorkspace,
												"input", id, fieldName))
												.getAbsolutePath());
							} else {
								// file
								props.put(
										fieldName,
										new File(FileUtil.path(localWorkspace,
												"input", id, fieldName,
												entryName)).getAbsolutePath());
							}

						}

					}

				} else {

					if (item.getFieldName().startsWith("input-")) {
						String key = item.getFieldName().replace("input-", "");

						String value = Streams.asString(item.openStream());
						if (!props.containsKey(key)) {
							// don't override uploaded files
							props.put(key, value);
						}

					}

				}

			}

		} catch (Exception e) {
			e.printStackTrace();
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
				job.setApplication(app.getName() + " " + app.getVersion());
				job.setApplicationId(tool);
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

	private FileItemIterator parseRequest(Representation entity)
			throws FileUploadException, IOException {

		// 1/ Create a factory for disk-based file items
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(1000240);

		// 2/ Create a new file upload handler based on the Restlet
		// FileUpload extension that will parse Restlet requests and
		// generates FileItems.
		RestletFileUpload upload = new RestletFileUpload(factory);

		return upload.getItemIterator(entity);

	}

}
