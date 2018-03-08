package cloudgene.mapred.api.v2.jobs;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadBase.FileUploadIOException;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.FileUtils;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.util.Application;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.PublicUser;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlParameterInput;
import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;

public class SubmitJob extends BaseResource {

	@Post
	public Representation post(Representation entity) {

		User user = getAuthUser();
		String appId = getAttribute("tool");

		Application application = getSettings().getAppByIdAndUser(appId, user);
		WdlApp app = null;
		try {
			app = application.getWdlApp();
		} catch (Exception e1) {

			return error404("Application '" + appId + "' not found or the request requires user authentication.");

		}

		if (app.getWorkflow() == null) {
			return error404("Application '" + appId + "' has no mapred section.");
		}

		WorkflowEngine engine = getWorkflowEngine();
		Settings settings = getSettings();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");
		String id = "job-" + sdf.format(new Date());

		boolean publicMode = false;

		if (user != null) {
			// private mode

			int maxPerUser = settings.getMaxRunningJobsPerUser();
			if (!user.isAdmin() && engine.getJobsByUser(user).size() >= maxPerUser) {
				return error400("Only " + maxPerUser + " jobs per user can be executed simultaneously.");
			}

		} else {

			// public mode
			user = PublicUser.getUser(getDatabase());

			String uuid = UUID.randomUUID().toString();
			id = id + "-" + HashUtil.getMD5(uuid);
			publicMode = true;

		}

		String hdfsWorkspace = HdfsUtil.path(getSettings().getHdfsWorkspace(), id);
		String localWorkspace = FileUtil.path(getSettings().getLocalWorkspace(), id);
		FileUtil.createDirectory(localWorkspace);

		Map<String, String> inputParams = null;

		try {
			inputParams = parseAndUpdateInputParams(entity, app, hdfsWorkspace, localWorkspace);
		} catch (FileUploadIOException e) {
			return error400("Upload limit reached.");
		}

		if (inputParams == null) {
			return error400("Error during input parameter parsing.");
		}

		String name = id;
		if (!publicMode) {
			if (inputParams.get("job-name") != null && !inputParams.get("job-name").trim().isEmpty()) {
				name = inputParams.get("job-name");
			}
		}

		CloudgeneJob job = new CloudgeneJob(user, id, app, inputParams);
		job.setId(id);
		job.setName(name);
		job.setLocalWorkspace(localWorkspace);
		job.setHdfsWorkspace(hdfsWorkspace);
		job.setSettings(getSettings());
		job.setRemoveHdfsWorkspace(getSettings().isRemoveHdfsWorkspace());
		job.setApplication(app.getName() + " " + app.getVersion());
		job.setApplicationId(appId);

		engine.submit(job);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", id);
		return ok("Your job was successfully added to the job queue.", params);

	}

	private Map<String, String> parseAndUpdateInputParams(Representation entity, WdlApp app, String hdfsWorkspace,
			String localWorkspace) throws FileUploadIOException {
		Map<String, String> props = new HashMap<String, String>();
		Map<String, String> params = new HashMap<String, String>();

		FileItemIterator iterator = null;
		try {
			iterator = parseRequest(entity);
		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
		// uploaded files
		try {
			while (iterator.hasNext()) {

				FileItemStream item = iterator.next();

				String name = item.getName();

				if (name != null) {

					File file = null;

					try {
						// file parameter
						// write local file
						String tmpFile = getSettings().getTempFilename(item.getName());
						file = new File(tmpFile);

						FileUtils.copyInputStreamToFile(item.openStream(), file);

						// import into hdfs
						String entryName = item.getName();

						// remove upload indentification!
						String fieldName = item.getFieldName().replace("-upload", "").replace("input-", "");

						boolean hdfs = false;
						boolean folder = false;

						for (WdlParameterInput input : app.getWorkflow().getInputs()) {
							if (input.getId().equals(fieldName)) {
								hdfs = input.isHdfs();
								folder = input.isFolder();
							}
						}

						if (hdfs) {

							String targetPath = HdfsUtil.path(hdfsWorkspace, fieldName);

							String target = HdfsUtil.path(targetPath, entryName);

							HdfsUtil.put(tmpFile, target);

							if (folder) {
								// folder
								props.put(fieldName, HdfsUtil.makeAbsolute(HdfsUtil.path(hdfsWorkspace, fieldName)));
							} else {
								// file
								props.put(fieldName,
										HdfsUtil.makeAbsolute(HdfsUtil.path(hdfsWorkspace, fieldName, entryName)));
							}

						} else {

							// copy to workspace in temp directory
							String targetPath = FileUtil.path(localWorkspace, "input", fieldName);

							FileUtil.createDirectory(targetPath);

							String target = FileUtil.path(targetPath, entryName);

							FileUtil.copy(tmpFile, target);

							if (folder) {
								// folder
								props.put(fieldName, new File(targetPath).getAbsolutePath());
							} else {
								// file
								props.put(fieldName, new File(target).getAbsolutePath());
							}

						}

						// deletes temporary file
						FileUtil.deleteFile(tmpFile);

					} catch (FileUploadIOException e) {
						file.delete();
						throw e;
					} catch (Exception e) {
						file.delete();
						return null;

					}

				} else {

					if (item.getFieldName().startsWith("input-")) {
						String key = item.getFieldName().replace("input-", "");

						String value = Streams.asString(item.openStream());
						if (!props.containsKey(key)) {
							// don't override uploaded files
							props.put(key, value);
						}

					} else {
						String key = item.getFieldName();
						String value = Streams.asString(item.openStream());
						if (!params.containsKey(key)) {
							// don't override uploaded files
							params.put(key, value);
						}
					}

				}

			}
		} catch (FileUploadIOException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
		for (WdlParameterInput input : app.getWorkflow().getInputs()) {
			if (props.containsKey(input.getId())) {
				if (input.getType().equals("checkbox")) {
					params.put(input.getId(), input.getValues().get("true"));
				} else {
					params.put(input.getId(), props.get(input.getId()));
				}
			} else {
				// ignore invisible input parameters
				if (input.getType().equals("checkbox") && input.isVisible()) {
					params.put(input.getId(), input.getValues().get("false"));
				}
			}
		}

		return params;
	}

	private FileItemIterator parseRequest(Representation entity) throws FileUploadException, IOException {

		// 1/ Create a factory for disk-based file items
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(1000240);

		// 2/ Create a new file upload handler based on the Restlet
		// FileUpload extension that will parse Restlet requests and
		// generates FileItems.
		RestletFileUpload upload = new RestletFileUpload(factory);
		Settings settings = getSettings();
		upload.setFileSizeMax(settings.getUploadLimit());
		return upload.getItemIterator(entity);

	}
}
