package cloudgene.mapred.util;

import java.io.File;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.ClusterStatus;

import genepi.hadoop.HadoopUtil;
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

	public static boolean verifyCluster() throws Exception {

		String confFolder = HadoopCluster.getConf();

		if (confFolder == null) {
			throw new Exception("Please define a cluster in file settings.yaml.");
		}

		if (new File(confFolder).exists()) {
			String configName = "mapred-site.xml";
			String configFile = FileUtil.path(confFolder, configName);
			if (!new File(configFile).exists()) {
				throw new Exception("No '" + configName + "' file found in configuration folder '" + confFolder + "'.");
			}
		} else {
			throw new Exception("Configuration folder '" + confFolder + "' not found.");
		}

		ClusterStatus cluster = HadoopUtil.getInstance().getClusterDetails();
		if (cluster.getActiveTrackerNames().isEmpty()) {
			throw new Exception("No active nodes founds.");
		}
		Configuration configuration = HdfsUtil.getConfiguration();
		FileSystem fileSystem = FileSystem.get(configuration);

		try {
			Path path = fileSystem.getHomeDirectory();

			if (fileSystem.exists(path)) {
				return true;
			} else {
				throw new Exception(
						"Home directory '" + path + "' for user " + HadoopCluster.getUsername() + " not found.");
			}

		} catch (Exception e) {
			throw new Exception(
					"Problems find the home directory for user " + HadoopCluster.getUsername() + ". " + e.getMessage());
		}
	}

}
