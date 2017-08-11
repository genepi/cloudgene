package cloudgene.mapred.wdl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cloudgene.mapred.util.ApplicationInstaller;
import cloudgene.mapred.util.Settings;
import genepi.hadoop.HdfsUtil;

public class WdlApp extends WdlHeader implements Comparable<WdlApp> {

	private WdlMapReduce mapred;

	private Map<String, String> cluster;

	private List<Map<String, Object>> installation;

	private List<Map<String, Object>> deinstallation;

	public WdlMapReduce getMapred() {
		return mapred;
	}

	public void setMapred(WdlMapReduce mapred) {
		this.mapred = mapred;
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

	public void install(HashMap<String, String> environment) throws IOException {
		if (installation != null) {
			ApplicationInstaller.install(installation, environment);
		}
	}

	public void deinstall(HashMap<String, String> environment) throws IOException {
		if (deinstallation != null) {
			ApplicationInstaller.install(deinstallation, environment);
			HdfsUtil.delete(environment.get("hdfs_app_folder"));
		}
	}

	@Override
	public int compareTo(WdlApp o) {
		return getName().compareTo(o.getName());
	}

}
