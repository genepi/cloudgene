package cloudgene.mapred.util.junit;

import genepi.hadoop.HdfsUtil;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.mapred.ClusterStatus;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MiniMRCluster;

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

		if (cluster == null) {

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
			System.setProperty("hadoop.log.dir", "test-log-dir");
			MiniMRCluster mrCluster = new MiniMRCluster(1, fs.getUri()
					.toString(), 1, null, null, new JobConf(conf));
			JobConf mrClusterConf = mrCluster.createJobConf();
			HdfsUtil.setDefaultConfiguration(new Configuration(mrClusterConf));

			System.out.println("------");

			JobClient client = new JobClient(mrClusterConf);
			ClusterStatus status = client.getClusterStatus(true);
			System.out.println(status.getActiveTrackerNames());
		}
	}

}
