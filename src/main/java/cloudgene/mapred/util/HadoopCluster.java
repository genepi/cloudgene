package cloudgene.mapred.util;

import java.io.File;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;

public class HadoopCluster {

	private static String username;

	private static String conf;

	private static String name;

	public static void setConfPath(String name, String path, String username) {
		if (username != null) {
			System.setProperty("HADOOP_USER_NAME", username);
			HadoopCluster.username = username;
		}
		HadoopCluster.conf = path;
		HadoopCluster.name = name;
		if (new File(path).exists()) {
			Configuration configuration = new Configuration();
			// add all xml files from hadoop conf folder to default
			// configuration
			String[] xmlFiles = FileUtil.getFiles(path, "*.xml");
			for (String xmlFile : xmlFiles) {
				configuration.addResource(new Path(xmlFile));
			}

			HdfsUtil.setDefaultConfiguration(configuration);
		}

	}

	public static String getUsername() {
		return username;
	}

	public static String getConf() {
		return conf;
	}

	public static String getName() {
		return name;
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
