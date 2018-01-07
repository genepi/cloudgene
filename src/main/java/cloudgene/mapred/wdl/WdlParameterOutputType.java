package cloudgene.mapred.wdl;

public enum WdlParameterOutputType {
	LOCAL_FOLDER("local-folder"), LOCAL_FILE("local-file"), HDFS_FOLDER("hdfs-folder"), HDFS_FILE("hdfs-file");

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
			if (v.getValue().equalsIgnoreCase(value))
				return v;
		throw new IllegalArgumentException("Value '" + value + "' is not a valid type.");
	}
}
