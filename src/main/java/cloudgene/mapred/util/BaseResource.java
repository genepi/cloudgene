package cloudgene.mapred.util;


import java.net.URLDecoder;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
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
import cloudgene.mapred.jobs.WorkflowEngine;
import genepi.db.Database;

public class BaseResource extends ServerResource {

	private WebApp application;

	private Database database;
		
	@Override
	protected void doInit() throws ResourceException {
		super.doInit();

		application = (WebApp) getApplication();
		database = application.getDatabase();
		
		String secretKey = getSettings().getSecretKey();
		if (secretKey == null || secretKey.isEmpty() || secretKey.equals(Settings.DEFAULT_SECURITY_KEY)) {
			secretKey = RandomStringUtils.randomAlphabetic(64);
			getSettings().setSecretKey(secretKey);
			getSettings().save();
		}

			
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

		User user = JWTUtil.getUserByRequest(getDatabase(), getRequest(), getSettings().getSecretKey(), checkCsrf);
		if (user != null) {
			return user;
		} else {
			return null;
		}

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
		//encode automatically
		String value = super.getAttribute(name);
		if (value != null) {
			return URLDecoder.decode(value);
		}else{
			return null;
		}
	}
	
	
	@Override
	public String getQueryValue(String name) { 
		String value = super.getQueryValue(name);
		if (value != null){
			return URLDecoder.decode(value);
		}else{
			return null;
		}
	}

	public Representation error(Status status, String message) {

		setStatus(status, message);

		JSONObject jsonObject = new JSONObject();

		try {

			jsonObject.put("success", false);
			jsonObject.put("message", message);

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
			jsonObject.put("message", message);

		} catch (JSONException e) {

			setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
			return new EmptyRepresentation();

		}
		return new JsonRepresentation(jsonObject);

	}

	public Representation ok(String message, Map<String, Object> params) {

		setStatus(Status.SUCCESS_OK, message);

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

}