package cloudgene.mapred.server.responses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlParameterInput;
import cloudgene.mapred.wdl.WdlParameterInputType;

public class WdlParameterInputResponse {

	private String id;
	private String description;
	private String type;
	private String bind;
	private String value;
	private boolean visible;
	private boolean required;
	private boolean adminOnly;
	private String help;
	private String pattern;
	private String accept;
	private String details;
	private String source;
	private String emptySelection;
	private List<PropertyResponse> values;
	private String key;
	private String label;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public boolean isAdminOnly() {
		return adminOnly;
	}

	public void setAdminOnly(boolean adminOnly) {
		this.adminOnly = adminOnly;
	}

	public String getHelp() {
		return help;
	}

	public void setHelp(String help) {
		this.help = help;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getAccept() {
		return accept;
	}

	public void setAccept(String accept) {
		this.accept = accept;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getEmptySelection() {
		return emptySelection;
	}

	public void setEmptySelection(String emptySelection) {
		this.emptySelection = emptySelection;
	}
	
	public List<PropertyResponse> getValues() {
		return values;
	}

	public void setValues(List<PropertyResponse> values) {
		this.values = values;
	}

	public String getBind() {
		return bind;
	}

	public void setBind(String bind) {
		this.bind = bind;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}


	public static WdlParameterInputResponse build(WdlParameterInput input, List<WdlApp> apps) {
		WdlParameterInputResponse response = new WdlParameterInputResponse();
		response.setId(input.getId());
		response.setDescription(input.getDescription());
		response.setType(input.getTypeAsEnum().toString());

		if (input.getValue() != null) {
			response.setValue(input.getValue());
		}
		response.setVisible(input.isVisible());
		response.setRequired(input.isRequired());
		response.setAdminOnly(input.isAdminOnly());
		response.setHelp(input.getHelp());

		if (input.getPattern() != null && !input.getPattern().isEmpty()) {
			response.setPattern(input.getPattern());
		}

		if (input.getAccept() != null) {
			response.setAccept(input.getAccept());
		}

		if (input.getDetails() != null) {
			response.setDetails(input.getDetails());
		}

		if (input.isFolder()) {
			response.setSource("upload");
		}

		if (input.getEmptySelection() != null) {
			response.setEmptySelection(input.getEmptySelection());
		}

		if (input.getTypeAsEnum() == WdlParameterInputType.LIST && input.hasDataBindung()) {
			String category = input.getValues().get("category");
			String property = input.getValues().get("property");
			String bind = input.getValues().get("bind");
			List<PropertyResponse> propertyResponses = new ArrayList<PropertyResponse>();

			for (WdlApp app : apps) {
				if (category != null && !category.isEmpty()) {

					if (app.getCategory() != null && app.getCategory().equals(category)) {
						Map values = (Map) app.getProperties().get(property);
						PropertyResponse propertyResponse = PropertyResponse.build("apps@" + app.getId(), app.getName(),
								values);
						propertyResponses.add(propertyResponse);
					}

				} else {
					// TODO:!!
				}

			}
			response.setValues(propertyResponses);
			response.setBind(bind);
			response.setType("binded_list");
			return response;
		}

		if (input.getTypeAsEnum() == WdlParameterInputType.LIST
				|| input.getTypeAsEnum() == WdlParameterInputType.CHECKBOX
				|| input.getTypeAsEnum() == WdlParameterInputType.RADIO) {
			Map<String, String> values = input.getValues();
			List<String> keys = new ArrayList<String>(values.keySet());
			Collections.sort(keys);
			List<PropertyResponse> propertyResponses = new ArrayList<PropertyResponse>();
			for (String key : keys) {
				String value = values.get(key);
				PropertyResponse propertyResponse = PropertyResponse.build(key, value);
				propertyResponses.add(propertyResponse);
			}
			response.setValues(propertyResponses);
		}

		if (input.getTypeAsEnum() == WdlParameterInputType.APP_LIST) {
			List<PropertyResponse> propertyResponses = new ArrayList<PropertyResponse>();
			for (WdlApp app : apps) {
				String category = input.getCategory();

				if (category != null && !category.isEmpty()) {
					// filter by category
					if (app.getCategory() != null && app.getCategory().equals(category)) {
						PropertyResponse propertyResponse = PropertyResponse.build("apps@" + app.getId(),
								app.getName());
						propertyResponses.add(propertyResponse);
					}

				} else {
					PropertyResponse propertyResponse = PropertyResponse.build("apps@" + app.getId(), app.getName());
					propertyResponses.add(propertyResponse);
				}

			}
			response.setValues(propertyResponses);
		}

		return response;
	}

	public static List<WdlParameterInputResponse> build(List<WdlParameterInput> inputs, List<WdlApp> apps) {
		List<WdlParameterInputResponse> response = new Vector<WdlParameterInputResponse>();

		for (WdlParameterInput input : inputs) {
			response.add(WdlParameterInputResponse.build(input, apps));
		}
		return response;
	}

}
