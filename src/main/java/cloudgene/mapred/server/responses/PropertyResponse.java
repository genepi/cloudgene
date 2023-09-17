package cloudgene.mapred.server.responses;

import java.util.List;
import java.util.Map;
import java.util.Vector;

public class PropertyResponse {
	String key;
	String value;
	String label;
	private List<PropertyResponse> values;
	boolean enabled;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public List<PropertyResponse> getValues() {
		return values;
	}

	public void setValues(List<PropertyResponse> values) {
		this.values = values;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public static PropertyResponse build(String key, String value) {
		PropertyResponse response = new PropertyResponse();
		response.setKey(key);
		response.setValue(value);
		return response;
	}

	public static List<PropertyResponse> buildWithValues(Map values) {
		List<PropertyResponse> response = new Vector<PropertyResponse>();

		for (Object key : values.keySet()) {
			String value = values.get(key).toString();
			response.add(PropertyResponse.build(key.toString(), value));
		}

		return response;
	}

	public static List<PropertyResponse> buildWithValues(List<Map> values) {
		List<PropertyResponse> response = new Vector<PropertyResponse>();

		for (Map object : values) {
			if (object.containsKey("id") && object.containsKey("name")) {
				System.out.println(object);
				response.add(PropertyResponse.build(object.get("id").toString(), object.get("name").toString()));
			}
		}

		return response;
	}

	public static PropertyResponse build(String key, String value, Object values) {
		PropertyResponse response = new PropertyResponse();
		response.setKey(key);
		response.setLabel(value);
		if (values instanceof Map) {
			List<PropertyResponse> responseWithValues = buildWithValues((Map) values);
			response.setValues(responseWithValues);
		} else if (values instanceof List) {
			List<PropertyResponse> responseWithValues = buildWithValues((List) values);
			response.setValues(responseWithValues);
		}
		return response;
	}

}
