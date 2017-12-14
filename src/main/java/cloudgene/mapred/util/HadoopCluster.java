package cloudgene.mapred.util;

import org.apache.hadoop.conf.Configuration;

import genepi.hadoop.HdfsUtil;

public class HadoopCluster {
	public static void init(String host, String username) {
		if (username != null) {
			System.setProperty("HADOOP_USER_NAME", username);
		}
		if (host != null) {
			Configuration configuration = new Configuration();
			configuration.set("fs.defaultFS", "hdfs://" + host + ":8020");
			configuration.set("mapred.job.tracker", host + ":8021");
			HdfsUtil.setDefaultConfiguration(configuration);
		}
	}

	public static String getJobTracker() {
		Configuration configuration = HdfsUtil.getConfiguration();
		return configuration.get("mapred.job.tracker");
	}

	public static String getDefaultFS() {
		Configuration configuration = HdfsUtil.getConfiguration();
		return configuration.get("fs.defaultFS");
	}

}
