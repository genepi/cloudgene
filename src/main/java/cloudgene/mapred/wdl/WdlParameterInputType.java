package cloudgene.mapred.wdl;

public enum WdlParameterInputType {
	LOCAL_FOLDER("local-folder"), LOCAL_FILE("local-file"), HDFS_FOLDER("hdfs-folder"), HDFS_FILE("hdfs-file"), TEXT(
			"text"), STRING("string"), CHECKBOX("checkbox"), LIST(
					"list"), NUMBER("number"), LABEL("label"), AGBCHECKBOX("agbcheckbox"), GROUP("group");

	private String value;

	WdlParameterInputType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return this.getValue();
	}

	public static WdlParameterInputType getEnum(String value) {
		for (WdlParameterInputType v : values())
			if (v.getValue().equalsIgnoreCase(value))
				return v;
		throw new IllegalArgumentException("Value '" + value + "' is not a valid type.");
	}
}
