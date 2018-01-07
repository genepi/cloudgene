package cloudgene.mapred.wdl;

public class WdlParameterOutput implements WdlParameter {

	private String id;

	private String description;

	private WdlParameterOutputType type;

	private boolean makeAbsolute = true;

	private boolean download = true;

	private boolean autoExport = false;

	private boolean mergeOutput = true;

	private boolean zip = true;

	private boolean removeHeader = true;

	private boolean adminOnly = false;

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

	@Deprecated
	public String getType() {
		return type.toString();
	}

	public void setType(String type) {
		this.type = WdlParameterOutputType.getEnum(type);
	}

	public WdlParameterOutputType getTypeAsEnum() {
		return type;
	}

	public boolean isMergeOutput() {
		return mergeOutput;
	}

	public void setMergeOutput(boolean mergeOutput) {
		this.mergeOutput = mergeOutput;
	}

	public boolean isDownload() {
		return download;
	}

	public void setDownload(boolean download) {
		this.download = download;
	}

	public boolean isMakeAbsolute() {
		return makeAbsolute;
	}

	public void setMakeAbsolute(boolean absolute) {
		this.makeAbsolute = absolute;
	}

	@Deprecated
	public boolean isTemp() {
		return false;
	}

	@Deprecated
	public void setTemp(boolean temp) {
	}

	public void setZip(boolean zip) {
		this.zip = zip;
	}

	public boolean isZip() {
		return zip;
	}

	public void setRemoveHeader(boolean removeHeader) {
		this.removeHeader = removeHeader;
	}

	public boolean isRemoveHeader() {
		return removeHeader;
	}

	public void setAutoExport(boolean autoExport) {
		this.autoExport = autoExport;
	}

	public boolean isAutoExport() {
		return autoExport;
	}

	public void setAdminOnly(boolean adminOnly) {
		this.adminOnly = adminOnly;
	}

	public boolean isAdminOnly() {
		return adminOnly;
	}

	public boolean isFileOrFolder() {
		return (type == WdlParameterOutputType.HDFS_FILE || type == WdlParameterOutputType.HDFS_FOLDER
				|| type == WdlParameterOutputType.LOCAL_FILE || type == WdlParameterOutputType.LOCAL_FOLDER);
	}

	public boolean isHdfs() {
		return (type == WdlParameterOutputType.HDFS_FOLDER || type == WdlParameterOutputType.HDFS_FILE);
	}

	public boolean isFolder() {
		return (type == WdlParameterOutputType.HDFS_FOLDER || type == WdlParameterOutputType.LOCAL_FOLDER);
	}

}
