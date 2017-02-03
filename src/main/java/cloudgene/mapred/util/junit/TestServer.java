package cloudgene.mapred.util.junit;

import java.io.File;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;
import org.restlet.engine.Engine;
import org.restlet.ext.slf4j.Slf4jLoggerFacade;

import cloudgene.mapred.Main;
import cloudgene.mapred.WebServer;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.TemplateDao;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.jobs.PersistentWorkflowEngine;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.util.Application;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.util.Template;
import genepi.db.Database;
import genepi.db.DatabaseUpdater;
import genepi.db.h2.H2Connector;
import genepi.io.FileUtil;
import junit.framework.Test;

public class TestServer {

	public static int PORT = 8085;

	public static final String HOSTNAME = "http://localhost:" + PORT;

	protected WebServer server;

	protected User user;

	protected Settings settings = new Settings();

	private Database database;

	private WorkflowEngine engine;

	private List<Application> applications;

	private Thread engineThread;

	private static TestServer instance;

	private TestServer() {
		settings.getMail().put("port", TestMailServer.PORT+"");
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

			settings.setApps(applications);

		}

		return applications;

	}

	public static TestServer getInstance() {
		if (instance == null) {
			instance = new TestServer();
		}
		return instance;

	}

	public Database createDatabase(boolean newDatabase) throws SQLException {

		if (database != null) {
			return database;
		}

		// delete old database
		if (newDatabase){
			FileUtil.deleteDirectory("test-database");
		}

		H2Connector connector = new H2Connector("./test-database/mapred", "mapred", "mapred", false);
		// DatabaseConnector connector = new MySqlConnector("localhost", "3306",
		// "cloudgene",
		// "root", "lukas");
		database = new Database();

		try {

			database.connect(connector);

			if (connector.isNewDatabase()) {

				// init schema
				InputStream is = Main.class.getResourceAsStream("/create-tables.sql");
				connector.executeSQL(is);

				File versionFile = new File("version.txt");
				if (versionFile.exists()) {
					versionFile.delete();
				}
			}

			InputStream is = Main.class.getResourceAsStream("/updates.sql");
			DatabaseUpdater askimedUpdater = new DatabaseUpdater(connector, "version.txt", is, Main.VERSION);
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
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);

		}
		return database;

	}

	public WorkflowEngine startWorkflowEngineWithoutServer() throws SQLException {
		if (engine == null) {
			database = createDatabase(true);
			// start workflow engine
			engine = new PersistentWorkflowEngine(database, 1, 1);
			engineThread = new Thread(engine);
			engineThread.start();
		}
		return engine;
	}

	public void start() throws SQLException {
		start(true);
	}

	public void start(boolean newDatabase) throws SQLException {

		if (server == null) {

			registerApplications();

			database = createDatabase(newDatabase);

			// inser messages
			TemplateDao htmlSnippetDao = new TemplateDao(database);

			for (Template defaultSnippet : Template.SNIPPETS) {

				Template snippet = htmlSnippetDao.findByKey(defaultSnippet.getKey());
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
					webAppFolder = FileUtil
							.path(new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath())
									.getParent(), "html", "webapp");
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
		start(false);

	}

	public User getUser() {
		return user;
	}

	public Settings getSettings() {
		return settings;
	}

	public Database getDatabase() {
		return database;
	}

}
