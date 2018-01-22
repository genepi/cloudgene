package cloudgene.mapred.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;

public class HadoopCluster {

	public static void setConfPath(String path, String username) {
		if (username != null) {
			System.setProperty("HADOOP_USER_NAME", username);
		}
		Configuration configuration = new Configuration();
		// add all xml files from hadoop conf folder to default configuration
		String[] xmlFiles = FileUtil.getFiles(path, "*.xml");
		for (String xmlFile : xmlFiles) {
			configuration.addResource(new Path(xmlFile));
		}
		HdfsUtil.setDefaultConfiguration(configuration);

	}

	public static void setHostname(String host, String username) {
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
