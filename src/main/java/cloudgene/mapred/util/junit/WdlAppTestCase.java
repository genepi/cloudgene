package cloudgene.mapred.util.junit;

import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import org.junit.BeforeClass;

import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlApp;

public class WdlAppTestCase {

	private static WorkflowEngine engine;

	public static String LOCAL_WORKSPACE = "test-cluster-local";

	public static String HDFS_WORKSPACE = "test-cluster-hdfs";

	@BeforeClass
	public static void startServers() throws IOException, SQLException {

		// start mini cluster
		TestCluster.getInstance().start();

		// start cloudgene workfloe engine
		engine = TestServer.getInstance().startWorkflowEngineWithoutServer();

	}

	public void execute(CloudgeneJob job) throws InterruptedException {
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(1000);
		}
		// Thread.sleep(4000);
	}

	public CloudgeneJob createJobFromWdl(WdlApp app, Map<String, String> inputs) throws Exception {

		User user = TestServer.getInstance().getAdminUser();
		Settings settings = TestServer.getInstance().getSettings();

		String id = "test_" + System.currentTimeMillis();

		String hdfsWorkspace = HdfsUtil.path(HDFS_WORKSPACE, id);
		String localWorkspace = FileUtil.path(LOCAL_WORKSPACE, id);
		FileUtil.createDirectory(localWorkspace);

		CloudgeneJob job = new CloudgeneJob(user, id, app, inputs);
		job.setId(id);
		job.setName(id);
		job.setLocalWorkspace(localWorkspace);
		job.setHdfsWorkspace(hdfsWorkspace);
		job.setSettings(settings);
		job.setRemoveHdfsWorkspace(true);
		job.setApplication(app.getName() + " " + app.getVersion());
		job.setApplicationId(app.getId());

		return job;
	}

}