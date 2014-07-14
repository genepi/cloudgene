package cloudgene.mapred.jobs;

import java.util.List;
import java.util.Vector;

import cloudgene.mapred.wdl.WdlParameter;

public class CloudgeneParameter {

	private int id;

	private String description;

	private String value = "";

	private String type = "";

	private boolean input;

	private boolean download = true;

	private String name = "";

	private String format = "";

	private List<Download> files;

	private CloudgeneJob job;

	private String jobId;

	private boolean autoExport = false;

	private boolean makeAbsolute = false;

	private boolean zip = false;

	private boolean mergeOutput = false;

	private boolean removeHeader = true;

	public CloudgeneParameter() {

	}

	public CloudgeneParameter(WdlParameter parameter) {
		setName(parameter.getId());
		setValue(parameter.getValue());
		setType(parameter.getType());
		setInput(parameter.isInput());
		setDownload(parameter.isDownload());
		setDescription(parameter.getDescription());
		setFormat(parameter.getFormat());
		setMakeAbsolute(parameter.isMakeAbsolute());
		setAutoExport(parameter.isAutoExport());
		setZip(parameter.isZip());
		setMergeOutput(parameter.isMergeOutput());
		setRemoveHeader(parameter.isRemoveHeader());
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isInput() {
		return input;
	}

	public void setInput(boolean input) {
		this.input = input;
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

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
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

}
