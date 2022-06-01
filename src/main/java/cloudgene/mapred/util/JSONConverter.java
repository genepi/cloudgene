package cloudgene.mapred.util;

import cloudgene.mapred.apps.Application;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneParameterOutput;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlParameterInput;
import cloudgene.mapred.wdl.WdlParameterInputType;
import genepi.io.FileUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JSONConverter {

	public static JSONObject convert(AbstractJob job) {

		JsonConfig config = new JsonConfig();
		config.setExcludes(new String[] { "user", "inputParams", "output", "error", "s3Url", "task", "config",
				"mapReduceJob", "job", "step", "context", "hdfsWorkspace", "localWorkspace", "logOutFiles",
				"removeHdfsWorkspace", "settings", "setupComplete", "stdOutFile", "workingDirectory", "parameter",
				"logOutFile", "map", "reduce", "mapProgress", "reduceProgress", "jobId", "makeAbsolute", "mergeOutput",
				"removeHeader", "value", "autoExport", "download", "tip", "apiToken", "parameterId", "count",
				"username" });

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
			System.out.println(property);
			for (WdlApp app : apps) {
				if (category != null && !category.isEmpty()) {
					// filter by category
					if (app.getCategory() != null && app.getCategory().equals(category)) {

						JSONObject valuesObject = new JSONObject();
						valuesObject.put("key", "apps@" + app.getId());
						valuesObject.put("label", app.getName());
						// TODO: check null and instance of map
						Map values = (Map) app.getProperties().get(property);
						JSONArray array2 = new JSONArray();
						for (Object key : values.keySet()) {
							JSONObject valuesObject2 = new JSONObject();
							String value = values.get(key).toString();
							valuesObject2.put("key", key.toString());
							valuesObject2.put("value", value);
							valuesObject2.put("enabled", false);
							array2.add(valuesObject2);
						}

						valuesObject.put("values", array2);
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

	public static JSONArray convertApplications(List<Application> applications) {
		JSONArray array = new JSONArray();
		for (Application application : applications) {
			array.add(convert(application));
		}
		return array;
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

}
