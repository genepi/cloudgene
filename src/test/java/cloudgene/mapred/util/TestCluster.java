package cloudgene.mapred.util;

import genepi.hadoop.HdfsUtil;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.hadoop.hdfs.MiniDFSCluster;

public class TestCluster {

	private static TestCluster instance;

	private static String WORKING_DIRECTORY = "test-cluster";

	private MiniDFSCluster cluster;

	private FileSystem fs;

	private Configuration conf;

	private TestCluster() {

	}

	public static TestCluster getInstance() {
		if (instance == null) {
			instance = new TestCluster();
		}
		return instance;
	}

	public void start() throws IOException {

		File testDataCluster1 = new File(WORKING_DIRECTORY);
		if (testDataCluster1.exists()) {
			testDataCluster1.delete();
		}
		conf = new HdfsConfiguration();
		conf.set(MiniDFSCluster.HDFS_MINIDFS_BASEDIR,
				testDataCluster1.getAbsolutePath());
		cluster = new MiniDFSCluster.Builder(conf).build();
		fs = cluster.getFileSystem();

		// set mincluster as default config
		HdfsUtil.setDefaultConfiguration(conf);
	}

}
