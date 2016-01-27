package cloudgene.mapred.util;

import genepi.io.FileUtil;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;
import org.restlet.engine.Engine;
import org.restlet.ext.slf4j.Slf4jLoggerFacade;

import cloudgene.mapred.Main;
import cloudgene.mapred.WebServer;
import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.database.H2Connector;
import cloudgene.mapred.database.TemplateDao;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.database.util.Database;
import cloudgene.mapred.database.util.DatabaseUpdater;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.util.Application;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.util.Template;

public class TestEnvironment {

	protected User user;

	protected Settings settings = new Settings();

	private Database database;

	private WorkflowEngine engine;

	private List<Application> applications;

	private Thread engineThread;

	private static TestEnvironment instance;

	private TestEnvironment() {

	}

	public List<Application> registerApplications() {

		if (applications == null) {

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

			
			settings.setApps(applications);

		}

		return applications;

	}

	public static TestEnvironment getInstance() {
		if (instance == null) {
			instance = new TestEnvironment();
		}
		return instance;

	}

	public Database createDatabase() throws SQLException {

		if (database != null) {
			return database;
		}

		H2Connector connector = new H2Connector("test-database/mapred",
				"mapred", "mapred", false);

		database = new Database();

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

		// insert user admin
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

	public WorkflowEngine startWorkflowEngineWithoutServer()
			throws SQLException {
		if (engine == null) {
			database = createDatabase();
			// start workflow engine
			engine = new WorkflowEngine(database, 1, 1);
			engineThread = new Thread(engine);
			engineThread.start();
		}
		return engine;
	}

	public static int PORT = 8085;

	public static final String HOSTNAME = "http://localhost:" + PORT;

	protected WebServer server;

	public void startWebServer() throws SQLException {

		if (server == null) {

			registerApplications();

			database = createDatabase();

			// inser messages
			TemplateDao htmlSnippetDao = new TemplateDao(database);

			for (Template defaultSnippet : Template.SNIPPETS) {

				Template snippet = htmlSnippetDao.findByKey(defaultSnippet
						.getKey());
				if (snippet == null) {
					htmlSnippetDao.insert(defaultSnippet);
				} else {
				}

			}

			try {

				// start workflow engine
				WorkflowEngine engine = startWorkflowEngineWithoutServer();

				PropertyConfigurator.configure("config/log4j.properties");

				Slf4jLoggerFacade loggerFacade = new Slf4jLoggerFacade();
				Engine.getInstance().setLoggerFacade(loggerFacade);

				String webAppFolder = "";

				if (new File("webapp").exists()) {
					webAppFolder = "webapp";
				} else {
					webAppFolder = FileUtil.path(new File(Main.class
							.getProtectionDomain().getCodeSource()
							.getLocation().getPath()).getParent(), "html",
							"webapp");
					System.out.println(webAppFolder);
				}

				String pagesFolder = "";
				if (new File("pages").exists()) {
					pagesFolder = "pages";
				} else {
					pagesFolder = "sample/pages";
				}

				server = new WebServer();
				server.setPort(PORT);
				server.setRootDirectory(webAppFolder);
				server.setPagesDirectory(pagesFolder);

				server.setDatabase(database);
				server.setSettings(settings);
				server.setWorkflowEngine(engine);
				server.setSessions(new UserSessions());

				server.start();

			} catch (Exception e) {

				database.disconnect();
				System.exit(1);

			}
		}

	}

	public void reStartWebServer() throws Exception {
		engine.stop();
		engineThread.stop();
		engine = null;
		database.disconnect();

		server.stop();
		server = null;
		database = null;
		startWebServer();

	}

	public User getUser() {
		return user;
	}

	public Settings getSettings() {
		return settings;
	}

}
