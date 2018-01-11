package cloudgene.mapred.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlParameterInput;
import cloudgene.mapred.wdl.WdlParameterInputType;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

public class JSONConverter {

	public static JSONObject convert(AbstractJob job) {

		JsonConfig config = new JsonConfig();
		config.setExcludes(new String[] { "user", "inputParams", "output", "error", "s3Url", "task", "config",
				"mapReduceJob", "job", "step", "context", "hdfsWorkspace", "localWorkspace", "logOutFiles",
				"removeHdfsWorkspace", "settings", "setupComplete", "stdOutFile", "workingDirectory", "parameter",
				"logOutFile", "map", "reduce", "mapProgress", "reduceProgress", "jobId", "makeAbsolute", "mergeOutput",
				"removeHeader", "value", "autoExport", "adminOnly", "download", "tip", "apiToken", "parameterId",
				"count", "username" });
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
		return object;

	}

	public static JSONArray convert(List<WdlParameterInput> inputs, List<WdlApp> apps) {
		JSONArray array = new JSONArray();
		for (WdlParameterInput input : inputs) {
			if (input.isVisible()){
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
		if (input.getTypeAsEnum() == WdlParameterInputType.LIST
				|| input.getTypeAsEnum() == WdlParameterInputType.CHECKBOX) {
			JSONObject valuesObject = new JSONObject();

			Map<String, String> values = input.getValues();
			List<String> keys = new ArrayList<String>(values.keySet());
			Collections.sort(keys);
			for (String key : keys) {
				String value = values.get(key);
				valuesObject.put(key, value);
			}
			object.put("values", valuesObject);
		}

		if (input.getTypeAsEnum() == WdlParameterInputType.APP_LIST) {
			JSONObject valuesObject = new JSONObject();
			for (WdlApp app : apps) {
				String category = input.getCategory();
				if (category != null && !category.isEmpty()) {
					// filter by category
					if (app.getCategory() != null && app.getCategory().equals(category)) {
						valuesObject.put("apps@" + app.getId(), app.getName());
					}
				} else {
					valuesObject.put("apps@" + app.getId(), app.getName());
				}
			}
			object.put("values", valuesObject);
		}

		return object;

	}

}
