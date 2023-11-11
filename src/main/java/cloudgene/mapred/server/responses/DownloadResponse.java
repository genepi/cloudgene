package cloudgene.mapred.server.responses;

import java.util.List;
import java.util.Vector;

import cloudgene.mapred.jobs.Download;
import com.fasterxml.jackson.annotation.JsonClassDescription;

@JsonClassDescription
public class DownloadResponse {

	private String name;
	private String path;
	private String hash;
	private int count;
	private String size;
	private int parameterId;
	private String user;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public int getParameterId() {
		return parameterId;
	}

	public void setParameterId(int parameterId) {
		this.parameterId = parameterId;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
	
	public static DownloadResponse build(Download param) {
		DownloadResponse response = new DownloadResponse();
		response.setName(param.getName());
		response.setHash(param.getHash());
		response.setSize(param.getSize());
		response.setCount(param.getCount());
		response.setPath(param.getPath());
		return response;
	}

	public static List<DownloadResponse> build(List<Download> params) {
		List<DownloadResponse> response = new Vector<DownloadResponse>();
		for (Download param : params) {
			response.add(DownloadResponse.build(param));
		}
		return response;
	}

}
