package cloudgene.mapred.jobs;

import java.util.List;
import java.util.Vector;

import cloudgene.mapred.wdl.WdlParameterInput;
import cloudgene.mapred.wdl.WdlParameterOutput;
import cloudgene.mapred.wdl.WdlParameterOutputType;

public class CloudgeneParameterOutput {

	private int id;

	private String description;

	private String value = "";

	private WdlParameterOutputType type;

	private boolean download = true;

	private String name = "";

	private List<Download> files;

	private CloudgeneJob job;

	private String jobId;

	private boolean autoExport = false;

	private boolean makeAbsolute = false;

	private boolean zip = false;

	private boolean mergeOutput = false;

	private boolean removeHeader = true;

	private boolean adminOnly = false;

	public CloudgeneParameterOutput() {

	}

	public CloudgeneParameterOutput(WdlParameterOutput parameter) {
		setName(parameter.getId());
		setType(parameter.getTypeAsEnum());
		setDownload(parameter.isDownload());
		setDescription(parameter.getDescription());
		setMakeAbsolute(parameter.isMakeAbsolute());
		setAutoExport(parameter.isAutoExport());
		setZip(parameter.isZip());
		setMergeOutput(parameter.isMergeOutput());
		setRemoveHeader(parameter.isRemoveHeader());
		setAdminOnly(parameter.isAdminOnly());
		files = new Vector<Download>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
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

	public WdlParameterOutputType getType() {
		return type;
	}

	public void setType(WdlParameterOutputType type) {
		this.type = type;
	}

	public boolean isDownload() {
		return download;
	}

	public void setDownload(boolean download) {
		this.download = download;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Download> getFiles() {
		return files;
	}

	public void setFiles(List<Download> files) {
		this.files = files;
	}

	public void setJob(CloudgeneJob job) {
		this.job = job;
	}

	public CloudgeneJob getJob() {
		return job;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getJobId() {
		return jobId;
	}

	public boolean isMakeAbsolute() {
		return makeAbsolute;
	}

	public void setMakeAbsolute(boolean absolute) {
		this.makeAbsolute = absolute;
	}

	public void setAutoExport(boolean autoExport) {
		this.autoExport = autoExport;
	}

	public boolean isAutoExport() {
		return autoExport;
	}

	public void setZip(boolean zip) {
		this.zip = zip;
	}

	public void setMergeOutput(boolean mergeOutput) {
		this.mergeOutput = mergeOutput;
	}

	public void setRemoveHeader(boolean removeHeader) {
		this.removeHeader = removeHeader;
	}

	public boolean isMergeOutput() {
		return mergeOutput;
	}

	public boolean isRemoveHeader() {
		return removeHeader;
	}

	public boolean isZip() {
		return zip;
	}

	public void setAdminOnly(boolean adminOnly) {
		this.adminOnly = adminOnly;
	}

	public boolean isAdminOnly() {
		return adminOnly;
	}

}
