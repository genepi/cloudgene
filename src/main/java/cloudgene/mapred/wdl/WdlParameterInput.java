package cloudgene.mapred.wdl;

import com.fasterxml.jackson.annotation.JsonClassDescription;

import java.util.HashMap;
import java.util.Map;

@JsonClassDescription
public class WdlParameterInput implements WdlParameter {

	private String id;

	private String description;

	private String value = "";

	//needed, because yamlbeans expects property AND getter/setter methods.
	private String type;
	
	private WdlParameterInputType typeEnum;

	private boolean visible = true;

	private boolean required = true;

	public Map<String, String> values;

	private boolean adminOnly = false;

	private String help = null;

	private String category = null;

	private String accept = null;

	private String details = null;

	private String pattern = null;

	private String emptySelection = null;

	public WdlParameterInput() {
		values = new HashMap<String, String>();
		values.put("true", "true");
		values.put("false", "false");
	}

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

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Deprecated
	public String getType() {
		return typeEnum.toString();
	}

	public void setType(String type) {
		this.typeEnum = WdlParameterInputType.getEnum(type);
	}

	public WdlParameterInputType getTypeAsEnum() {
		return typeEnum;
	}

	public Map<String, String> getValues() {
		return values;
	}

	public void setValues(Map<String, String> values) {
		this.values = values;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public boolean isRequired() {
		return required;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setAdminOnly(boolean adminOnly) {
		this.adminOnly = adminOnly;
	}

	public boolean isAdminOnly() {
		return adminOnly;
	}

	public boolean isFileOrFolder() {
		return (typeEnum == WdlParameterInputType.HDFS_FILE || typeEnum == WdlParameterInputType.HDFS_FOLDER
				|| typeEnum == WdlParameterInputType.LOCAL_FILE || typeEnum == WdlParameterInputType.LOCAL_FOLDER);
	}

	public boolean isHdfs() {
		return (typeEnum == WdlParameterInputType.HDFS_FOLDER || typeEnum == WdlParameterInputType.HDFS_FILE);
	}

	public boolean isFolder() {
		return (typeEnum == WdlParameterInputType.HDFS_FOLDER || typeEnum == WdlParameterInputType.LOCAL_FOLDER);
	}

	public void setHelp(String help) {
		this.help = help;
	}

	public String getHelp() {
		return help;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getCategory() {
		return category;
	}

	public void setAccept(String accept) {
		this.accept = accept;
	}

	public String getAccept() {
		return accept;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String getDetails() {
		return details;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getPattern() {
		return pattern;
	}

	public void setEmptySelection(String emptySelection) {
		this.emptySelection = emptySelection;
	}

	public String getEmptySelection() {
		return emptySelection;
	}

	public boolean hasDataBindung() {
		if (value != null) {
			return values.containsKey("bind");
		} else {
			return false;
		}
	}

}
