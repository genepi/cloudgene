package cloudgene.mapred.wdl;

import java.util.Map;

public class WdlParameterInput implements WdlParameter {

	private String id;

	private String description;

	private String value = "";

	private WdlParameterInputType type;

	private boolean visible = true;

	private boolean required = true;

	public Map<String, String> values;

	private boolean adminOnly = false;

	private String help = null;

	private String category = null;

	private String accept = null;
	
	private String details = null;

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
		return type.toString();
	}

	public void setType(String type) {
		this.type = WdlParameterInputType.getEnum(type);
	}

	public WdlParameterInputType getTypeAsEnum() {
		return type;
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
		return (type == WdlParameterInputType.HDFS_FILE || type == WdlParameterInputType.HDFS_FOLDER
				|| type == WdlParameterInputType.LOCAL_FILE || type == WdlParameterInputType.LOCAL_FOLDER);
	}

	public boolean isHdfs() {
		return (type == WdlParameterInputType.HDFS_FOLDER || type == WdlParameterInputType.HDFS_FILE);
	}

	public boolean isFolder() {
		return (type == WdlParameterInputType.HDFS_FOLDER || type == WdlParameterInputType.LOCAL_FOLDER);
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

}
