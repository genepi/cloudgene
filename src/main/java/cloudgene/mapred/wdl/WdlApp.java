package cloudgene.mapred.wdl;

import java.util.Map;

public class WdlApp extends WdlHeader implements Comparable<WdlApp>{

	private WdlMapReduce mapred;

	private Map<String, String> cluster;

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

	@Override
	public int compareTo(WdlApp o) {
		return getName().compareTo(o.getName());
	}

}
