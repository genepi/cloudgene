package cloudgene.mapred.util;

import java.net.URLDecoder;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.WebApp;
import cloudgene.mapred.apps.ApplicationRepository;
import cloudgene.mapred.core.JWTUtil;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.util.Database;
import cloudgene.mapred.jobs.WorkflowEngine;

public class BaseResource extends ServerResource {
	private static final Log log = LogFactory.getLog(BaseResource.class);

	private WebApp application;

	private Database database;
	
	private boolean accessedByApi = false;
		
	@Override
	protected void doInit() throws ResourceException {
		super.doInit();

		application = (WebApp) getApplication();
		database = application.getDatabase();

	}

	public Database getDatabase() {
		return database;
	}

	public WebApp getWebApp() {
		return application;
	}

	public User getAuthUser() {
		return getAuthUser(true);
	}

	public User getAuthUser(boolean checkCsrf) {
		return JWTUtil.getUserByRequest(getDatabase(), getRequest(), getSettings().getSecretKey(), checkCsrf);
	}

	public User getAuthUserAndAllowApiToken() {
		return 	getAuthUserAndAllowApiToken(true);
	}
	
	public User getAuthUserAndAllowApiToken(boolean checkCsrf) {
		User user = JWTUtil.getUserByRequest(getDatabase(), getRequest(), getSettings().getSecretKey(), checkCsrf);
		if (user == null) {
			user = JWTUtil.getUserByApiToken(getDatabase(), getRequest(), getSettings().getSecretKey());

			if (user != null) {
				log.info(String.format("User: API Token Authentication for user: %s (ID %s - email %s)", user.getUsername(), user.getId(), user.getMail()));
			}

			accessedByApi = true;
		}
		return user;
	}
	
	public boolean isAccessedByApi() {
		// TODO: Is this threadsafe?
		return accessedByApi;
	}

	public Settings getSettings() {
		return application.getSettings();
	}

	public ApplicationRepository getApplicationRepository() {
		return application.getSettings().getApplicationRepository();
	}

	public WorkflowEngine getWorkflowEngine() {
		return application.getWorkflowEngine();
	}

	@Override
	public String getAttribute(String name) {
		// encode automatically
		String value = super.getAttribute(name);
		if (value != null) {
			return URLDecoder.decode(value);
		} else {
			return null;
		}
	}

	@Override
	public String getQueryValue(String name) {
		String value = super.getQueryValue(name);
		if (value != null) {
			return URLDecoder.decode(value);
		} else {
			return null;
		}
	}

	public Representation error(Status status, String message) {

		setStatus(status, message);

		JSONObject jsonObject = new JSONObject();

		try {

			jsonObject.put("success", false);
			jsonObject.put("message", StringEscapeUtils.escapeHtml(message));

		} catch (JSONException e) {

			setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
			return new EmptyRepresentation();

		}

		return new JsonRepresentation(jsonObject);

	}

	public Representation ok(String message) {

		setStatus(Status.SUCCESS_OK, message);

		JSONObject jsonObject = new JSONObject();

		try {

			jsonObject.put("success", true);
			jsonObject.put("message", StringEscapeUtils.escapeHtml(message));

		} catch (JSONException e) {

			setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
			return new EmptyRepresentation();

		}
		return new JsonRepresentation(jsonObject);

	}

	public Representation ok(String message, Map<String, Object> params) {

		setStatus(Status.SUCCESS_OK, StringEscapeUtils.escapeHtml(message));

		JSONObject jsonObject = new JSONObject();

		try {

			jsonObject.put("success", true);
			jsonObject.put("message", message);
			for (String key : params.keySet()) {
				jsonObject.put(key, params.get(key));
			}

		} catch (JSONException e) {

			setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
			return new EmptyRepresentation();

		}
		return new JsonRepresentation(jsonObject);

	}

	public Representation error401(String message) {
		return error(Status.CLIENT_ERROR_UNAUTHORIZED, message);
	}

	public Representation error403(String message) {
		return error(Status.CLIENT_ERROR_FORBIDDEN, message);
	}

	public Representation error404(String message) {
		return error(Status.CLIENT_ERROR_NOT_FOUND, message);
	}

	public Representation error400(String message) {
		return error(Status.CLIENT_ERROR_BAD_REQUEST, message);
	}

	public Representation error503(String message) {
		return error(Status.SERVER_ERROR_SERVICE_UNAVAILABLE, message);
	}

	
}