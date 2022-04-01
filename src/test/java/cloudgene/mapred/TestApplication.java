package cloudgene.mapred;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.esotericsoftware.yamlbeans.YamlException;

import cloudgene.mapred.apps.Application;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.util.Config;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.util.TestMailServer;
import genepi.db.Database;
import genepi.io.FileUtil;
import io.micronaut.context.annotation.Context;

@Context
public class TestApplication extends cloudgene.mapred.server.Application {

	public TestApplication() throws Exception {
		super();
	}

	@Override
	protected Settings loadSettings(Config config) throws FileNotFoundException, YamlException {
		Settings settings = new Settings(new Config());

		HashMap<String, String> mail = new HashMap<String, String>();
		mail.put("smtp", "localhost");
		mail.put("port", TestMailServer.PORT + "");
		mail.put("user", "");
		mail.put("password", "");
		mail.put("name", "noreply@cloudgene");
		settings.setMail(mail);

		// delete old database
		FileUtil.deleteDirectory("test-database");

		HashMap<String, String> database = new HashMap<String, String>();
		database.put("driver", "h2");
		database.put("database", "./test-database/mapred");
		database.put("user", "mapred");
		database.put("password", "mapred");
		settings.setDatabase(database);

		settings.setSecretKey(Settings.DEFAULT_SECURITY_KEY);

		// Set threads for workflow engine to 1
		settings.setThreadsQueue(1);
		settings.setThreadsSetupQueue(1);
		settings.setMaintenance(false);

		registerApplications(settings);

		return settings;
	}

	public List<Application> registerApplications(Settings settings) {

		List<Application> applications = new Vector<Application>();

		Application app = new Application();
		app.setId("return-true-step-public");
		app.setFilename("test-data/return-true.yaml");
		app.setPermission("public");
		applications.add(app);

		Application app2 = new Application();
		app2.setId("return-false-step-public");
		app2.setFilename("test-data/return-false.yaml");
		app2.setPermission("public");
		applications.add(app2);

		Application app3 = new Application();
		app3.setId("return-exception-step-public");
		app3.setFilename("test-data/return-exception.yaml");
		app3.setPermission("public");
		applications.add(app3);

		Application app4 = new Application();
		app4.setId("write-text-to-file");
		app4.setFilename("test-data/write-text-to-file.yaml");
		app4.setPermission("public");
		applications.add(app4);

		Application app5 = new Application();
		app5.setId("return-true-in-setup");
		app5.setFilename("test-data/return-true-in-setup.yaml");
		app5.setPermission("public");
		applications.add(app5);

		Application app6 = new Application();
		app6.setId("return-false-in-setup");
		app6.setFilename("test-data/return-false-in-setup.yaml");
		app6.setPermission("public");
		applications.add(app6);

		Application app7 = new Application();
		app7.setId("all-possible-inputs");
		app7.setFilename("test-data/all-possible-inputs.yaml");
		app7.setPermission("public");
		applications.add(app7);

		Application app71 = new Application();
		app71.setId("all-possible-inputs-private");
		app71.setFilename("test-data/all-possible-inputs.yaml");
		app71.setPermission("private");
		applications.add(app71);

		Application app8 = new Application();
		app8.setId("long-sleep");
		app8.setFilename("test-data/long-sleep.yaml");
		app8.setPermission("public");
		applications.add(app8);

		Application app9 = new Application();
		app9.setId("write-files-to-folder");
		app9.setFilename("test-data/write-files-to-folder.yaml");
		app9.setPermission("public");
		applications.add(app9);

		Application app13 = new Application();
		app13.setId("three-tasks");
		app13.setFilename("test-data/three-tasks.yaml");
		app13.setPermission("public");
		applications.add(app13);

		Application app14 = new Application();
		app14.setId("write-text-to-std-out");
		app14.setFilename("test-data/write-text-to-std-out.yaml");
		app14.setPermission("public");
		applications.add(app14);

		// hdfs

		Application app10 = new Application();
		app10.setId("all-possible-inputs-hdfs");
		app10.setFilename("test-data/all-possible-inputs-hdfs.yaml");
		app10.setPermission("public");
		applications.add(app10);

		Application app11 = new Application();
		app11.setId("write-files-to-hdfs-folder");
		app11.setFilename("test-data/write-files-to-hdfs-folder.yaml");
		app11.setPermission("public");
		applications.add(app11);

		Application app12 = new Application();
		app12.setId("write-text-to-hdfs-file");
		app12.setFilename("test-data/write-text-to-hdfs-file.yaml");
		app12.setPermission("public");
		applications.add(app12);

		Application app16 = new Application();
		app16.setId("sftp-import");
		app16.setFilename("test-data/sftp-import.yaml");
		app16.setPermission("public");
		applications.add(app16);

		Application app17 = new Application();
		app17.setId("app-links");
		app17.setFilename("test-data/app-links.yaml");
		app17.setPermission("public");
		applications.add(app17);

		Application app18 = new Application();
		app18.setId("app-links-child");
		app18.setFilename("test-data/app-links-child.yaml");
		app18.setPermission("public");
		applications.add(app18);

		Application app188 = new Application();
		app188.setId("app-links-child-version@1.0.0");
		app188.setFilename("test-data/app-links-child.yaml");
		app188.setPermission("public");
		applications.add(app188);

		Application app19 = new Application();
		app19.setId("app-links-child-protected");
		app19.setFilename("test-data/app-links-child.yaml");
		app19.setPermission("protected");
		applications.add(app19);

		Application app20 = new Application();
		app20.setId("app-installation2");
		app20.setFilename("test-data/app-installation2.yaml");
		app20.setPermission("public");
		applications.add(app20);

		Application app21 = new Application();
		app21.setId("app-installation-child");
		app21.setFilename("test-data/app-installation-child.yaml");
		app21.setPermission("public");
		applications.add(app21);

		Application app22 = new Application();
		app22.setId("print-hidden-inputs");
		app22.setFilename("test-data/print-hidden-inputs.yaml");
		app22.setPermission("public");
		applications.add(app22);

		Application app23 = new Application();
		app23.setId("app-version-test@1.0.1");
		app23.setFilename("test-data/app-version-test.yaml");
		app23.setPermission("private");
		applications.add(app23);

		Application app24 = new Application();
		app24.setId("app-version-test@1.2.1");
		app24.setFilename("test-data/app-version-test2.yaml");
		app24.setPermission("private");
		applications.add(app24);

		settings.setApps(applications);

		return applications;

	}

	@Override
	protected void afterDatabaseConnection(Database database) {

		String username = "admin";
		String password = "admin1978";

		// insert user admin
		UserDao dao = new UserDao(database);
		User adminUser = dao.findByUsername(username);
		if (adminUser == null) {
			adminUser = new User();
			adminUser.setUsername(username);
			password = HashUtil.hashPassword(password);
			adminUser.setPassword(password);
			adminUser.makeAdmin();
			dao.insert(adminUser);
		}

		String usernameUser = "user";
		String passwordUser = "admin1978";

		// insert user admin
		User user = dao.findByUsername(usernameUser);
		if (user == null) {
			user = new User();
			user.setUsername(usernameUser);
			password = HashUtil.hashPassword(passwordUser);
			user.setPassword(passwordUser);
			user.setRoles(new String[] { "public" });
			dao.insert(user);
		}

		User userPublic = dao.findByUsername("public");
		if (userPublic == null) {
			userPublic = new User();
			userPublic.setUsername("public");
			password = HashUtil.hashPassword("public");
			userPublic.setPassword(password);
			userPublic.setRoles(new String[] { "public" });
			dao.insert(userPublic);
		}

	}

}
