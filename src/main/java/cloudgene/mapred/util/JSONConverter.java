package cloudgene.mapred.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlParameterInput;
import cloudgene.mapred.wdl.WdlParameterInputType;

public class JSONConverter {

	public static ObjectNode convert(WdlApp app) {

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode object = mapper.createObjectNode();
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

	public static ArrayNode convert(List<WdlParameterInput> inputs, List<WdlApp> apps) {
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode array = mapper.createArrayNode();
		for (WdlParameterInput input : inputs) {
			if (input.isVisible()) {
				array.add(convert(input, apps));
			}
		}

		return array;
	}

	public static ObjectNode convert(WdlParameterInput input, List<WdlApp> apps) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode object = mapper.createObjectNode();
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
			mapper = new ObjectMapper();
			ArrayNode array = mapper.createArrayNode();
			String category = input.getValues().get("category");
			String property = input.getValues().get("property");
			String bind = input.getValues().get("bind");
			for (WdlApp app : apps) {
				if (category != null && !category.isEmpty()) {
					// filter by category
					if (app.getCategory() != null && app.getCategory().equals(category)) {
						ObjectNode valuesObject = mapper.createObjectNode();
						valuesObject.put("key", "apps@" + app.getId());
						valuesObject.put("label", app.getName());
						// TODO: check null and instance of map
						Map values = (Map) app.getProperties().get(property);
						ArrayNode array2 = mapper.createArrayNode();
						for (Object key : values.keySet()) {
							ObjectNode valuesObject2 = mapper.createObjectNode();
							String value = values.get(key).toString();
							valuesObject2.put("key", key.toString());
							valuesObject2.put("value", value);
							valuesObject2.put("enabled", false);
							array2.add(valuesObject2);
						}

						valuesObject.putPOJO("values", array2);
						array.add(valuesObject);

					}
				} else {
					// TODO:!!
				}
			}
			object.putPOJO("values", array);
			object.put("bind", bind);
			object.put("type", "binded_list");
			return object;
		}

		if (input.getTypeAsEnum() == WdlParameterInputType.LIST
				|| input.getTypeAsEnum() == WdlParameterInputType.CHECKBOX
				|| input.getTypeAsEnum() == WdlParameterInputType.RADIO) {
			ArrayNode array = mapper.createArrayNode();
			Map<String, String> values = input.getValues();
			List<String> keys = new ArrayList<String>(values.keySet());
			Collections.sort(keys);
			for (String key : keys) {
				ObjectNode valuesObject = mapper.createObjectNode();
				String value = values.get(key);
				valuesObject.put("key", key);
				valuesObject.put("value", value);
				array.add(valuesObject);
			}
			object.putPOJO("values", array);
			return object;
		}

		if (input.getTypeAsEnum() == WdlParameterInputType.APP_LIST) {
			ArrayNode array = mapper.createArrayNode();
			for (WdlApp app : apps) {
				String category = input.getCategory();
				if (category != null && !category.isEmpty()) {
					// filter by category
					if (app.getCategory() != null && app.getCategory().equals(category)) {
						ObjectNode valuesObject = mapper.createObjectNode();
						valuesObject.put("key", "apps@" + app.getId());
						valuesObject.put("value", app.getName());
						array.add(valuesObject);
					}
				} else {
					ObjectNode valuesObject = mapper.createObjectNode();
					valuesObject.put("key", "apps@" + app.getId());
					valuesObject.put("value", app.getName());
					array.add(valuesObject);
				}
			}
			object.putPOJO("values", array);
		}

		return object;

	}

}
