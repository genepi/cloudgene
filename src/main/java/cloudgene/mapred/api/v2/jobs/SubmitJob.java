package cloudgene.mapred.api.v2.jobs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

import org.apache.commons.fileupload.FileUploadBase.FileUploadIOException;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import cloudgene.mapred.apps.Application;
import cloudgene.mapred.apps.ApplicationRepository;
import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.PublicUser;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlParameterInput;
import cloudgene.mapred.wdl.WdlParameterInputType;
import genepi.db.Database;
import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.multipart.CompletedPart;
import io.micronaut.http.server.multipart.MultipartBody;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import reactor.core.publisher.Mono;

@Controller
public class SubmitJob {

	@Inject
	protected cloudgene.mapred.Application application;

	private static final Log log = LogFactory.getLog(SubmitJob.class);

	@Post(uri = "/api/v2/jobs/submit/{appId}", consumes = MediaType.MULTIPART_FORM_DATA, produces = MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	@Secured(SecurityRule.IS_ANONYMOUS)

	public Publisher<Object> submit(String appId, @Body MultipartBody body, @Nullable Principal principal) {

		return Mono.create(emitter -> {
			body.subscribe(new Subscriber<CompletedPart>() {

				List<FormParameter> form = new Vector<FormParameter>();

				private Subscription s;

				@Override
				public void onSubscribe(Subscription s) {
					this.s = s;
					s.request(1);
				}

				@Override
				public void onNext(CompletedPart completedPart) {
					String partName = completedPart.getName();
					if (completedPart instanceof CompletedFileUpload) {
						String originalFileName = ((CompletedFileUpload) completedPart).getFilename();
						String tmpFile = application.getSettings().getTempFilename(originalFileName);
						File file = new File(tmpFile);

						try {
							InputStream stream = completedPart.getInputStream();
							FileUtils.copyInputStreamToFile(stream, file);
							stream.close();

						} catch (IOException e) {
							e.printStackTrace();
						}
						form.add(new FormParameter(partName, file));

					} else {
						try {
							String value = Streams.asString(completedPart.getInputStream());
							log.info(partName + " --> " + value);
							form.add(new FormParameter(partName, value));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					s.request(1);
				}

				@Override
				public void onError(Throwable t) {
					emitter.error(t);
				}

				@Override
				public void onComplete() {
					String result = submit(appId, form, principal);
					emitter.success(result);
				}
			});
		});

	}

	public String submit(String appId, List<FormParameter> form, @Nullable Principal principal) {

		WorkflowEngine engine = this.application.getWorkflowEngine();
		Settings settings = this.application.getSettings();
		Database database = this.application.getDatabase();

		User user = application.getUserByPrincipal(principal);

		if (settings.isMaintenance() && !user.isAdmin()) {
			throw new HttpStatusException(HttpStatus.SERVICE_UNAVAILABLE,
					"This functionality is currently under maintenance.");
		}

		// TODO: still needed?
		try {
			appId = java.net.URLDecoder.decode(appId, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e2) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND, "Application '" + appId + "' is not in valid format.");
		}

		ApplicationRepository repository = settings.getApplicationRepository();
		Application application = repository.getByIdAndUser(appId, user);
		WdlApp app = null;
		try {
			app = application.getWdlApp();
		} catch (Exception e1) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND,
					"Application '" + appId + "' not found or the request requires user authentication.");
		}

		if (app.getWorkflow() == null) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND, "Application '" + appId + "' has no workflow section.");
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");
		String id = "job-" + sdf.format(new Date());

		boolean publicMode = false;

		if (user != null) {
			// private mode

			int maxPerUser = settings.getMaxRunningJobsPerUser();
			if (!user.isAdmin() && engine.getJobsByUser(user).size() >= maxPerUser) {
				throw new HttpStatusException(HttpStatus.BAD_REQUEST,
						"Only " + maxPerUser + " jobs per user can be executed simultaneously.");
			}

		} else {

			// public mode
			user = PublicUser.getUser(database);

			String uuid = UUID.randomUUID().toString();
			id = id + "-" + HashUtil.getSha256(uuid);
			publicMode = true;

		}

		String hdfsWorkspace = "";

		try {
			hdfsWorkspace = HdfsUtil.path(settings.getHdfsWorkspace(), id);
		} catch (NoClassDefFoundError e) {
			log.warn("Hadoop not found in classpath. Ignore HDFS Workspace.", e);
		}

		String localWorkspace = FileUtil.path(settings.getLocalWorkspace(), id);
		FileUtil.createDirectory(localWorkspace);

		Map<String, String> inputParams = null;

		try {
			inputParams = parseAndUpdateInputParams(form, app, hdfsWorkspace, localWorkspace);
		} catch (FileUploadIOException e) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Upload limit reached.");
		}

		if (inputParams == null) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Error during input parameter parsing.");
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
		job.setSettings(settings);
		job.setRemoveHdfsWorkspace(settings.isRemoveHdfsWorkspace());
		job.setApplication(app.getName() + " " + app.getVersion());
		job.setApplicationId(appId);

		// String userAgent = getRequest().getClientInfo().getAgent();
		// TODO: How to read userAgent from micronaut request!
		String userAgent = "Web.Interface";
		job.setUserAgent(userAgent);

		engine.submit(job);

		JSONObject jsonObject = new JSONObject();

		try {

			jsonObject.put("success", true);
			jsonObject.put("message", "Your job was successfully added to the job queue.");
			jsonObject.put("id", id);
		} catch (Exception e) {
			// TODO: handle exception
		}

		return jsonObject.toString();

	}

	private Map<String, String> parseAndUpdateInputParams(List<FormParameter> form, WdlApp app, String hdfsWorkspace,
			String localWorkspace) throws FileUploadIOException {

		Map<String, String> props = new HashMap<String, String>();
		Map<String, String> params = new HashMap<String, String>();

		// uploaded files
		try {
			for (FormParameter formParam : form) {

				String name = formParam.name;
				Object value = formParam.value;

				if (value instanceof File) {
					File inputFile = (File) value;

					try {

						String entryName = inputFile.getName();

						// remove upload indentification!
						String fieldName = name.replace("-upload", "").replace("input-", "");

						WdlParameterInput inputParam = null;
						for (WdlParameterInput input : app.getWorkflow().getInputs()) {
							if (input.getId().equals(fieldName)) {
								inputParam = input;
							}
						}

						if (inputParam.isHdfs()) {

							String targetPath = HdfsUtil.path(hdfsWorkspace, fieldName);

							String target = HdfsUtil.path(targetPath, entryName);

							HdfsUtil.put(inputFile.getAbsolutePath(), target);

							if (inputParam.isFolder()) {
								// folder
								props.put(fieldName, HdfsUtil.makeAbsolute(HdfsUtil.path(hdfsWorkspace, fieldName)));
							} else {
								// file
								props.put(fieldName,
										HdfsUtil.makeAbsolute(HdfsUtil.path(hdfsWorkspace, fieldName, entryName)));
							}

						} else {

							// copy to workspace in input directory
							String targetPath = FileUtil.path(localWorkspace, "input", fieldName);
							FileUtil.createDirectory(targetPath);

							String target = FileUtil.path(targetPath, entryName);

							FileUtil.copy(inputFile.getAbsolutePath(), target);

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
						FileUtil.deleteFile(inputFile.getAbsolutePath());

					} catch (Exception e) {
						FileUtil.deleteFile(inputFile.getAbsolutePath());
						throw e;
					}

				} else {

					String key = name;
					if (key.startsWith("input-")) {
						key = key.replace("input-", "");
					}
					if (!props.containsKey(key)) {
						// don't override uploaded files
						props.put(key, value.toString());
					}

				}

			}
		} catch (Exception e) {
			throw e;

		}
		try {
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

			params.put("job-name", props.get("job-name"));

		} catch (Exception e) {
			e.printStackTrace();
			throw e;

		}

		return params;
	}

	class FormParameter {
		public String name;

		public Object value;

		public FormParameter(String name, Object value) {
			this.name = name;
			this.value = value;
		}
	}

}
