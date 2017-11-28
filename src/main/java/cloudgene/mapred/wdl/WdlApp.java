package cloudgene.mapred.wdl;

import java.util.List;
import java.util.Map;

public class WdlApp extends WdlHeader implements Comparable<WdlApp> {

	private WdlWorkflow workflow;
	
	private Map<String, String> cluster;

	private List<Map<String, Object>> installation;

	private List<Map<String, Object>> deinstallation;
	
	private Map<String, String> properties;
	
	@Deprecated
	public WdlWorkflow getMapred() {
		return null;
	}

	@Deprecated
	public void setMapred(WdlWorkflow mapred) {
		this.workflow = mapred;
	}
	
	
	public WdlWorkflow getWorkflow() {
		return workflow;
	}
	
	public void setWorkflow(WdlWorkflow workflow) {
		this.workflow = workflow;
	}

	public Map<String, String> getCluster() {
		return cluster;
	}

	public void setCluster(Map<String, String> cluster) {
		this.cluster = cluster;
	}

	public void setInstallation(List<Map<String, Object>> installation) {
		this.installation = installation;
	}

	public List<Map<String, Object>> getInstallation() {
		return installation;
	}

	public void setDeinstallation(List<Map<String, Object>> deinstallation) {
		this.deinstallation = deinstallation;
	}

	public List<Map<String, Object>> getDeinstallation() {
		return deinstallation;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	
	public Map<String, String> getProperties() {
		return properties;
	}
		
	@Override
	public int compareTo(WdlApp o) {
		return getName().compareTo(o.getName());
	}

}
