package cloudgene.mapred.jobs;

import java.util.List;
import java.util.Vector;

import cloudgene.mapred.util.HashUtil;
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

	private List<JobResultsTreeItem> tree;

	private CloudgeneJob job;

	private String jobId;

	private boolean adminOnly = false;

	private String hash = "";

	public CloudgeneParameterOutput() {

	}

	public CloudgeneParameterOutput(WdlParameterOutput parameter) {
		setName(parameter.getId());
		setType(parameter.getTypeAsEnum());
		setDownload(parameter.isDownload());
		setDescription(parameter.getDescription());
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

	public void setAdminOnly(boolean adminOnly) {
		this.adminOnly = adminOnly;
	}

	public boolean isAdminOnly() {
		return adminOnly;
	}

	public void setTree(List<JobResultsTreeItem> tree) {
		this.tree = tree;
	}

	public List<JobResultsTreeItem> getTree() {
		return tree;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getHash() {
		return hash;
	}
	
	public String createHash() {
		String hash = "";
		for (Download download: files) {
			hash += download.getHash();
		}
		return HashUtil.getSha256(hash);
	}

}
