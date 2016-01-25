package cloudgene.mapred.jobs.util;

import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;

import java.io.File;
import java.sql.SQLException;
import java.util.Map;

import junit.framework.TestCase;
import cloudgene.mapred.Main;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.H2Connector;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.database.util.Database;
import cloudgene.mapred.database.util.DatabaseUpdater;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlApp;

public class CloudgeneTestCase extends TestCase {

	protected User user;

	protected Settings settings = new Settings();

	public Database createDatabase() throws SQLException {

		H2Connector connector = new H2Connector("test-database/mapred",
				"mapred", "mapred", false);

		Database database = new Database();

		try {

			database.connect(connector);

		} catch (SQLException e) {

			System.exit(1);

		}

		// change??
		if (connector.isNewDatabase()) {
			File versionFile = new File("version.txt");
			if (versionFile.exists()) {
				versionFile.delete();
			}
		}

		DatabaseUpdater askimedUpdater = new DatabaseUpdater(connector,
				"version.txt", "/updates.sql", Main.VERSION, false);
		if (askimedUpdater.needUpdate()) {
			if (!askimedUpdater.update()) {
				database.disconnect();
				System.exit(1);
			}
		}

		String username = "admin";
		String password = "admin1978";

		// insert user
		UserDao dao = new UserDao(database);
		user = dao.findByUsername(username);
		if (user == null) {
			user = new User();
			user.setUsername(username);
			password = HashUtil.getMD5(password);
			user.setPassword(password);
			user.setRole("admin");
			dao.insert(user);
		}

		return database;

	}

	public WorkflowEngine startWorkflowEngine() throws SQLException {
		Database database = createDatabase();
		// start workflow engine
		WorkflowEngine engine = new WorkflowEngine(database, 1, 1);
		return engine;
	}

	public CloudgeneJob createJobFromWdl(WdlApp app, Map<String, String> inputs)
			throws Exception {

		String id = "test_" + System.currentTimeMillis();

		String hdfsWorkspace = HdfsUtil.path(settings.getHdfsWorkspace(), id);
		String localWorkspace = FileUtil.path(settings.getLocalWorkspace(), id);

		CloudgeneJob job = new CloudgeneJob(user, id, app.getMapred(), inputs);
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
