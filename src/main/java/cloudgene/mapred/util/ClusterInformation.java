package cloudgene.mapred.util;

import org.apache.hadoop.mapred.ClusterStatus;
import org.apache.hadoop.util.VersionInfo;

public class ClusterInformation {

	private ClusterStatus status;
	
	private String version;
	
	private String date;

	public ClusterInformation(ClusterStatus status) {
		this.status = status;
	}

	public ClusterStatus getStatus() {
		return status;
	}

	public void setStatus(ClusterStatus status) {
		this.status = status;
	}
	
	public String getVersion() {
		return VersionInfo.getVersion();
	}
	
	public String getDate() {
		return VersionInfo.getDate();
	}
	
	public void setDate(String date) {
		this.date = date;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
}
