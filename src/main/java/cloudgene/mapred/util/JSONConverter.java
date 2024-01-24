package cloudgene.mapred.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import cloudgene.mapred.apps.Application;
import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneParameterOutput;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlParameterInput;
import cloudgene.mapred.wdl.WdlParameterInputType;
import genepi.io.FileUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

public class JSONConverter {

	public static final String MESSAGE_VALID_TOKEN = "API Token was created by %s and is valid until %s.";

	public static final String MESSAGE_EXPIRED_TOKEN = "API Token was created by %s and expired on %s.";

	public static final String MESSAGE_INVALID_TOKEN = "API Token was created with a previous version and is therefore invalid. Please recreate.";

	public static JSONObject convert(AbstractJob job) {

		JsonConfig config = new JsonConfig();
		config.setExcludes(new String[]{"user", "inputParams", "output", "error", "s3Url", "task", "config",
				"mapReduceJob", "job", "step", "context", "hdfsWorkspace", "localWorkspace", "logOutFiles",
				"removeHdfsWorkspace", "settings", "setupComplete", "stdOutFile", "workingDirectory", "parameter",
				"logOutFile", "map", "reduce", "mapProgress", "reduceProgress", "jobId", "makeAbsolute", "mergeOutput",
				"removeHeader", "value", "autoExport", "download", "tip", "apiToken", "parameterId", "count",
				"username"});

		// create tree
		for (CloudgeneParameterOutput param : job.getOutputParams()) {
			String hash = param.createHash();
			param.setHash(hash);
			param.setTree(JobResultsTreeUtil.createTree(param.getFiles()));
		}

		return JSONObject.fromObject(job, config);
	}

	public static JSONObject convert(WdlApp app) {

		JSONObject object = new JSONObject();
		object.put("id", app.getId());
		object.put("name", app.getName());
		object.put("version", app.getVersion());
		object.put("description", app.getDescription());
		object.put("author", app.getAuthor());
		object.put("website", app.getWebsite());
		if (app.getLogo() != null && !app.getLogo().isEmpty()) {
			object.put("logo", app.getLogo());
		}
		object.put("submitButton", app.getSubmitButton());
		return object;

	}

	public static JSONArray convert(List<WdlParameterInput> inputs, List<WdlApp> apps) {
		JSONArray array = new JSONArray();
		for (WdlParameterInput input : inputs) {
			if (input.isVisible()) {
				array.add(convert(input, apps));
			}
		}

		return array;
	}

	public static JSONObject convert(WdlParameterInput input, List<WdlApp> apps) {

		JSONObject object = new JSONObject();
		object.put("id", input.getId());
		object.put("description", input.getDescription());
		object.put("type", input.getTypeAsEnum().toString());
		if (input.getValue() != null) {
			object.put("value", input.getValue());
		}
		object.put("visible", input.isVisible());
		object.put("required", input.isRequired());
		object.put("adminOnly", input.isAdminOnly());
		object.put("help", input.getHelp());
		if (input.getPattern() != null && !input.getPattern().isEmpty()) {
			object.put("pattern", input.getPattern());
		}

		if (input.getAccept() != null) {
			object.put("accept", input.getAccept());
		}

		if (input.getDetails() != null) {
			object.put("details", input.getDetails());
		}

		if (input.isFolder()) {
			object.put("source", "upload");
		}

		if (input.getEmptySelection() != null) {
			object.put("emptySelection", input.getEmptySelection());
		}

		if (input.getTypeAsEnum() == WdlParameterInputType.LIST && input.hasDataBindung()) {
			JSONArray array = new JSONArray();
			String category = input.getValues().get("category");
			String property = input.getValues().get("property");
			String bind = input.getValues().get("bind");
			for (WdlApp app : apps) {
				if (category != null && !category.isEmpty()) {
					// filter by category
					if (app.getCategory() != null && app.getCategory().equals(category)) {

						JSONObject valuesObject = new JSONObject();
						valuesObject.put("key", "apps@" + app.getId());
						valuesObject.put("label", app.getName());
						// TODO: check null and instance of map
						Object values = app.getProperties().get(property);
						if (values instanceof Map){
							JSONArray array2 = buildFromMap((Map)values);
							valuesObject.put("values", array2);
						} else if (values instanceof List){
							JSONArray array2 = buildFromList((List)values);
							valuesObject.put("values", array2);
						}
						array.add(valuesObject);

					}
				} else {
					// TODO:!!
				}
			}
			object.put("values", array);
			object.put("bind", bind);
			object.put("type", "binded_list");
			return object;
		}

		if (input.getTypeAsEnum() == WdlParameterInputType.LIST
				|| input.getTypeAsEnum() == WdlParameterInputType.CHECKBOX
				|| input.getTypeAsEnum() == WdlParameterInputType.RADIO) {
			JSONArray array = new JSONArray();
			Map<String, String> values = input.getValues();
			List<String> keys = new ArrayList<String>(values.keySet());
			Collections.sort(keys);
			for (String key : keys) {
				JSONObject valuesObject = new JSONObject();
				String value = values.get(key);
				valuesObject.put("key", key);
				valuesObject.put("value", value);
				array.add(valuesObject);
			}
			object.put("values", array);
			return object;
		}

		if (input.getTypeAsEnum() == WdlParameterInputType.APP_LIST) {
			JSONArray array = new JSONArray();
			for (WdlApp app : apps) {
				String category = input.getCategory();
				if (category != null && !category.isEmpty()) {
					// filter by category
					if (app.getCategory() != null && app.getCategory().equals(category)) {
						JSONObject valuesObject = new JSONObject();
						valuesObject.put("key", "apps@" + app.getId());
						valuesObject.put("value", app.getName());
						array.add(valuesObject);
					}
				} else {
					JSONObject valuesObject = new JSONObject();
					valuesObject.put("key", "apps@" + app.getId());
					valuesObject.put("value", app.getName());
					array.add(valuesObject);
				}
			}
			object.put("values", array);
		}

		return object;

	}

	public static JSONArray convertUsers(List<User> users) {
		JSONArray array = new JSONArray();
		for (User user : users) {
			array.add(convert(user));
		}
		return array;
	}

	public static JSONArray convertApplications(List<Application> applications) {
		JSONArray array = new JSONArray();
		for (Application application : applications) {
			array.add(convert(application));
		}
		return array;
	}

	public static JSONObject convert(User user) {

		JSONObject object = new JSONObject();
		object.put("id", user.getId());
		object.put("username", user.getUsername());
		object.put("fullName", user.getFullName());
		if (user.getLastLogin() != null) {
			object.put("lastLogin", user.getLastLogin().toString());
		} else {
			object.put("lastLogin", "");
		}
		if (user.getLockedUntil() != null) {
			if (user.getLockedUntil().after(new Date())) {
				object.put("lockedUntil", user.getLockedUntil().toString());
			} else {
				object.put("lockedUntil", "");
			}
		} else {
			object.put("lockedUntil", "");
		}
		object.put("active", user.isActive());
		object.put("loginAttempts", user.getLoginAttempts());
		object.put("role", String.join(User.ROLE_SEPARATOR, user.getRoles()).toLowerCase());
		object.put("mail", user.getMail());
		object.put("admin", user.isAdmin());
		boolean hasApiToken = user.getApiToken() != null && !user.getApiToken().isEmpty();
		object.put("hasApiToken", hasApiToken);
		if (hasApiToken) {
			if (user.getApiTokenExpiresOn() == null) {
				object.put("apiTokenValid", false);
				object.put("apiTokenMessage", MESSAGE_INVALID_TOKEN);
			} else if (user.getApiTokenExpiresOn().getTime() > System.currentTimeMillis()) {
				object.put("apiTokenValid", true);
				object.put("apiTokenMessage",
						String.format(MESSAGE_VALID_TOKEN, user.getUsername(), user.getApiTokenExpiresOn()));
			} else {
				object.put("apiTokenValid", false);
				object.put("apiTokenMessage",
						String.format(MESSAGE_EXPIRED_TOKEN, user.getUsername(), user.getApiTokenExpiresOn()));
			}
		}

		return object;

	}

	public static JSONObject convert(Application application) {
		JSONObject object = new JSONObject();
		object.put("id", application.getId());
		object.put("enabled", application.isEnabled());
		object.put("filename", application.getFilename());
		object.put("loaded", application.isLoaded());
		object.put("errorMessage", application.getErrorMessage());
		object.put("changed", application.isChanged());
		object.put("permission", application.getPermission());
		WdlApp wdlApp = application.getWdlApp();
		object.put("wdlApp", wdlApp);
		if (new File(application.getFilename()).exists()) {
			object.put("source", FileUtil.readFileAsString(application.getFilename()));
		}
		return object;
	}

	private static JSONArray buildFromMap(Map values){
		JSONArray array = new JSONArray();
		for(Object key :values.keySet()){
			JSONObject valuesObject2 = new JSONObject();
			String value = values.get(key).toString();
			valuesObject2.put("key", key.toString());
			valuesObject2.put("value", value);
			valuesObject2.put("enabled", false);
			array.add(valuesObject2);
		}
		return array;
	}

	private static JSONArray buildFromList(List<Map> values){
		JSONArray array = new JSONArray();
		for (Map object : values) {
			if (object.containsKey("id") && object.containsKey("name")) {
				JSONObject valuesObject2 = new JSONObject();
				valuesObject2.put("key",object.get("id").toString());
				valuesObject2.put("value", object.get("name").toString());
				valuesObject2.put("enabled", false);
				array.add(valuesObject2);
			}
		}
		return array;
	}

}
