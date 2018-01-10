package cloudgene.mapred.wdl;

public enum WdlParameterOutputType {
	LOCAL_FOLDER("local_folder"), LOCAL_FILE("local_file"), HDFS_FOLDER("hdfs_folder"), HDFS_FILE("hdfs_file");

	private String value;

	WdlParameterOutputType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return this.getValue();
	}

	public static WdlParameterOutputType getEnum(String value) {
		for (WdlParameterOutputType v : values())
			if (v.getValue().equalsIgnoreCase(value.replaceAll("-", "_")))
				return v;
		throw new IllegalArgumentException("Value '" + value + "' is not a valid type.");
	}
}
