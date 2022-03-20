package cloudgene.mapred.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import cloudgene.mapred.TestApplication;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlReader;
import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;

@MicronautTest
public class WrongWorkspaceTest {

	@Inject
	TestApplication application;

	@Test
	public void testReturnTrueStep() throws Exception {

		WorkflowEngine engine = application.getWorkflowEngine();

		WdlApp app = WdlReader.loadAppFromFile("test-data/return-true.yaml");

		Map<String, String> inputs = new HashMap<String, String>();
		inputs.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, inputs);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(1000);
		}
		Thread.sleep(10000);

		JobDao dao = new JobDao(application.getDatabase());

		AbstractJob jobFromDb = dao.findById(job.getId());

		assertEquals(AbstractJob.STATE_FAILED, jobFromDb.getState());

		assertEquals(AbstractJob.STATE_FAILED, job.getState());
	}

	public CloudgeneJob createJobFromWdl(WdlApp app, Map<String, String> inputs) throws Exception {

		UserDao userDao = new UserDao(application.getDatabase());
		User user = userDao.findByUsername("user");

		Settings settings = application.getSettings();

		String id = "test_" + System.currentTimeMillis();

		String hdfsWorkspace = HdfsUtil.path("/gsfgdfgdf/vdadsadwa", id);
		String localWorkspace = FileUtil.path("/gsfgdfgdf/vdadsadwa", id);
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
