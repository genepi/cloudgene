package cloudgene.mapred.api.v2.jobs;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
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
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

import cloudgene.mapred.apps.Application;
import cloudgene.mapred.apps.ApplicationRepository;
import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.PublicUser;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlParameterInput;
import cloudgene.mapred.wdl.WdlParameterInputType;
import genepi.hadoop.HdfsUtil;
import genepi.hadoop.importer.ImporterFactory;
import genepi.io.FileUtil;

public class SubmitJob extends BaseResource {

	private static final Log log = LogFactory.getLog(SubmitJob.class);
	
	private static final String PARAM_JOB_NAME = "job-name";

	@Post
	public Representation post(Representation entity) {

		User user = getAuthUserAndAllowApiToken();

		if (getSettings().isMaintenance() && !user.isAdmin()) {
			return error503("This functionality is currently under maintenance.");
		}

		String appId = getAttribute("tool");
		try {
			appId = java.net.URLDecoder.decode(appId, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e2) {
			return error404("Application '" + appId + "' is not in valid format.");
		}

		ApplicationRepository repository = getApplicationRepository();
		Application application = repository.getByIdAndUser(appId, user);
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
			id = id + "-" + HashUtil.getSha256(uuid);
			publicMode = true;

		}

		String hdfsWorkspace = "";

		try {
			hdfsWorkspace = HdfsUtil.path(getSettings().getHdfsWorkspace(), id);
		} catch (NoClassDefFoundError e) {
			log.warn("Hadoop not found in classpath. Ignore HDFS Workspace.");
		}

		String localWorkspace = FileUtil.path(getSettings().getLocalWorkspace(), id);
		FileUtil.createDirectory(localWorkspace);

		Map<String, String> inputParams = null;

		try {
			inputParams = parseAndUpdateInputParams(entity, app, hdfsWorkspace, localWorkspace);
		} catch (FileUploadIOException e) {
			return error400("Upload limit reached.");
		} catch (FileUploadException e) {
			return error400(e.getMessage());
		}

		if (inputParams == null) {
			return error400("Error during input parameter parsing.");
		}

		String name = id;
		if (!publicMode) {
			if (inputParams.get(PARAM_JOB_NAME) != null && !inputParams.get(PARAM_JOB_NAME).trim().isEmpty()) {
				name = inputParams.get(PARAM_JOB_NAME);
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

		String userAgent = getRequest().getClientInfo().getAgent();
		job.setUserAgent(userAgent);

		engine.submit(job);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", id);

		String message = String.format("Job: Created job ID %s for user %s (ID %s - email %s)", id, user.getUsername(),
				user.getId(), user.getMail());
		if (this.isAccessedByApi()) {
			message += " (via API token)";
		}
		log.info(message);

		return ok("Your job was successfully added to the job queue.", params);

	}

	private Map<String, String> parseAndUpdateInputParams(Representation entity, WdlApp app, String hdfsWorkspace,
			String localWorkspace) throws FileUploadIOException, FileUploadException {
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

				String entryName = StringEscapeUtils.escapeHtml(item.getName());

				if (entryName != null) {

					File file = null;

					try {
						// file parameter
						// write local file
						String tmpFile = getSettings().getTempFilename(entryName);
						file = new File(tmpFile);

						FileUtils.copyInputStreamToFile(item.openStream(), file);

						// remove upload indentification!
						String fieldName = item.getFieldName().replace("-upload", "").replace("input-", "");

						// boolean hdfs = false;
						// boolean folder = false;

						WdlParameterInput inputParam = getInputParamByName(app, fieldName);

						if (inputParam == null) {
							throw new Exception("Parameter '" + fieldName + "' not found.");
						}

						if (inputParam.isHdfs()) {

							String targetPath = HdfsUtil.path(hdfsWorkspace, fieldName);

							String cleandEntryName = new File(entryName).getName();
							String target = HdfsUtil.path(targetPath, cleandEntryName);

							HdfsUtil.put(tmpFile, target);

							if (inputParam.isFolder()) {
								// folder
								props.put(fieldName, HdfsUtil.makeAbsolute(HdfsUtil.path(hdfsWorkspace, fieldName)));
							} else {
								// file
								props.put(fieldName, HdfsUtil
										.makeAbsolute(HdfsUtil.path(hdfsWorkspace, fieldName, cleandEntryName)));
							}

						} else {

							// copy to workspace in temp directory
							String targetPath = FileUtil.path(localWorkspace, "input", fieldName);

							FileUtil.createDirectory(targetPath);

							String cleandEntryName = new File(entryName).getName();
							String target = FileUtil.path(targetPath, cleandEntryName);

							FileUtil.copy(tmpFile, target);

							if (inputParam.isFolder()) {
								// folder
								if (inputParam.getPattern() != null && !inputParam.getPattern().isEmpty()) {
									props.put(fieldName, new File(targetPath).getAbsolutePath());
								} else {
									props.put(fieldName, new File(targetPath).getAbsolutePath());
								}
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

					String key = StringEscapeUtils.escapeHtml(item.getFieldName());
					if (key.startsWith("input-")) {
						key = key.replace("input-", "");
					}

					WdlParameterInput input = getInputParamByName(app, key);

					if (!key.equals(PARAM_JOB_NAME) && input == null) {
						throw new FileUploadException("Parameter '" + key + "' not found.");
					}

					String value = StringEscapeUtils.escapeHtml(Streams.asString(item.openStream()));

					if (input != null && input.isFileOrFolder() && ImporterFactory.needsImport(value)) {
						throw new FileUploadException("Parameter '" + input.getId()
								+ "': URL-based uploads are no longer supported. Please use direct file uploads instead.");
					}

					if (!props.containsKey(key)) {
						// don't override uploaded files
						props.put(key, value);
					}

				}

			}
		} catch (FileUploadIOException e) {
			throw e;
		} catch (FileUploadException e) {
			throw e;
		} catch (Exception e) {
			return null;

		}

		for (WdlParameterInput input : app.getWorkflow().getInputs()) {
			if (!params.containsKey(input.getId())) {
				if (props.containsKey(input.getId())) {

					if (input.isFolder() && input.getPattern() != null && !input.getPattern().isEmpty()) {
						String pattern = props.get(input.getId() + "-pattern");
						String value = props.get(input.getId());
						if (!value.endsWith("/")) {
							value = value + "/";
						}
						params.put(input.getId(), value + pattern);
					} else {

						if (input.getTypeAsEnum() == WdlParameterInputType.CHECKBOX) {
							params.put(input.getId(), input.getValues().get("true"));
						} else {
							params.put(input.getId(), props.get(input.getId()));
						}
					}
				} else {
					// ignore invisible input parameters
					if (input.getTypeAsEnum() == WdlParameterInputType.CHECKBOX && input.isVisible()) {
						params.put(input.getId(), input.getValues().get("false"));
					}
				}
			}
		}

		params.put(PARAM_JOB_NAME, props.get(PARAM_JOB_NAME));

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

	private WdlParameterInput getInputParamByName(WdlApp app, String name) {

		for (WdlParameterInput input : app.getWorkflow().getInputs()) {
			if (input.getId().equals(name)) {
				return input;
			}
		}
		return null;
	}

}
