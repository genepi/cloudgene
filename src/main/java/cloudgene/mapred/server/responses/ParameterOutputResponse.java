package cloudgene.mapred.server.responses;

import java.util.List;
import java.util.Vector;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;

import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.jobs.CloudgeneParameterOutput;
import cloudgene.mapred.jobs.JobResultsTreeItem;
import cloudgene.mapred.wdl.WdlParameterOutputType;

@JsonClassDescription
public class ParameterOutputResponse {

	private int id;

	private String description;

	private String value;

	private WdlParameterOutputType type;

	private boolean download;

	private String name;

	private List<JobResultsTreeItem> tree;

	private CloudgeneJob job;

	private String jobId;

	private boolean autoExport;

	private String hash;

	@JsonProperty("files")
	private List<DownloadResponse> downloadResponses;

	public static ParameterOutputResponse build(CloudgeneParameterOutput paramsOut) {
		ParameterOutputResponse response = new ParameterOutputResponse();
		response.setId(paramsOut.getId());
		response.setDescription(paramsOut.getDescription());
		response.setValue(paramsOut.getValue());
		response.setDownload(paramsOut.isDownload());
		response.setName(paramsOut.getName());
		response.setTree(paramsOut.getTree());
		response.setJobId(paramsOut.getJobId());
		response.setHash(paramsOut.getHash());

		List<DownloadResponse> responses = DownloadResponse.build(paramsOut.getFiles());
		response.setDownloadResponses(responses);

		return response;
	}

	public static List<ParameterOutputResponse> build(List<CloudgeneParameterOutput> params) {
		List<ParameterOutputResponse> response = new Vector<ParameterOutputResponse>();
		for (CloudgeneParameterOutput param : params) {
			response.add(ParameterOutputResponse.build(param));
		}
		return response;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<DownloadResponse> getDownloadResponses() {
		return downloadResponses;
	}

	public void setDownloadResponses(List<DownloadResponse> downloadResponses) {
		this.downloadResponses = downloadResponses;
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

	public List<JobResultsTreeItem> getTree() {
		return tree;
	}

	public void setTree(List<JobResultsTreeItem> tree) {
		this.tree = tree;
	}

	public CloudgeneJob getJob() {
		return job;
	}

	public void setJob(CloudgeneJob job) {
		this.job = job;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public boolean isAutoExport() {
		return autoExport;
	}

	public void setAutoExport(boolean autoExport) {
		this.autoExport = autoExport;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

}
