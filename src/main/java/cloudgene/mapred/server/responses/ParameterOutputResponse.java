package cloudgene.mapred.server.responses;

import java.util.List;
import java.util.Vector;

import com.fasterxml.jackson.annotation.JsonProperty;

import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.jobs.CloudgeneParameterOutput;
import cloudgene.mapred.jobs.JobResultsTreeItem;
import cloudgene.mapred.wdl.WdlParameterOutputType;

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

	private boolean makeAbsolute;

	private boolean zip;

	private boolean mergeOutput;

	private boolean removeHeader;

	private boolean adminOnly;

	private String hash = "";
	
	@JsonProperty("files")
	private List<DownloadResponse> downloadResponses;

	public static ParameterOutputResponse build(CloudgeneParameterOutput paramsOut) {
		ParameterOutputResponse response = new ParameterOutputResponse();
		response.setId(paramsOut.getId());
		response.setName(paramsOut.getName());
		
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

}
