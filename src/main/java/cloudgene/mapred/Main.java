package cloudgene.mapred;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.sql.SQLException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.engine.Engine;
//import org.restlet.ext.slf4j.Slf4jLoggerFacade;
import org.restlet.ext.slf4j.Slf4jLoggerFacade;

import com.esotericsoftware.yamlbeans.YamlReader;

import cloudgene.mapred.database.updates.BcryptHashUpdate;
import cloudgene.mapred.database.util.Database;
import cloudgene.mapred.database.util.DatabaseConnector;
import cloudgene.mapred.database.util.DatabaseConnectorFactory;
import cloudgene.mapred.database.util.DatabaseUpdater;
import cloudgene.mapred.database.util.Fixtures;
import cloudgene.mapred.jobs.PersistentWorkflowEngine;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.plugins.PluginManager;
import cloudgene.mapred.util.BuildUtil;
import cloudgene.mapred.util.Config;
import cloudgene.mapred.util.Settings;
import genepi.io.FileUtil;

public class Main {

	public static final String VERSION = "2.8.5";

	private Database database;

	private WebServer server;

	public void runCloudgene(Settings settings, String[] args) throws Exception {

		// configure logger

		Slf4jLoggerFacade loggerFacade = new Slf4jLoggerFacade();
		Engine.getInstance().setLoggerFacade(loggerFacade);

		Log log = LogFactory.getLog(Main.class);

		log.debug("Cloudgene " + VERSION);
		log.info(BuildUtil.getBuildInfos());

		// load cloudgene.conf file. contains path to settings, db, apps, ..
		Config config = new Config();
		if (new File(Config.CONFIG_FILENAME).exists()) {
			YamlReader reader = new YamlReader(new FileReader(Config.CONFIG_FILENAME));
			config = reader.read(Config.class);
		}

		String settingsFilename = config.getSettings();

		// load default settings when not yet loaded
		if (settings == null) {
			if (new File(settingsFilename).exists()) {
				log.info("Loading settings from " + settingsFilename + "...");
				settings = Settings.load(config);
			} else {
				settings = new Settings(config);
			}

			if (settings.getServerUrl() == null) {
				System.out.println("Error: serverUrl not set in settings.yaml");
				System.exit(1);
			}
			
			if (!settings.testPaths()) {
				System.exit(1);
			}
		}

		PluginManager pluginManager = PluginManager.getInstance();
		pluginManager.initPlugins(settings);

		// create the command line parser
		CommandLineParser parser = new DefaultParser();

		// create the Options
		Options options = new Options();
		Option portOption = new Option(null, "port", true, "runs cloudgene on port <PORT>");
		portOption.setRequired(false);
		portOption.setArgName("PORT");
		options.addOption(portOption);

		// parse the command line arguments
		CommandLine line = null;
		try {

			line = parser.parse(options, args);

		} catch (Exception e) {

			System.out.println(e.getMessage() + "\n");
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("hadoop jar cloudgene-mapred.jar", options);

			log.error("Parsing arguments failed", e);

			System.exit(1);

		}

		database = new Database();

		String secretKey = settings.getSecretKey();
		if (secretKey == null || secretKey.isEmpty() || secretKey.equals(Settings.DEFAULT_SECURITY_KEY)) {
			secretKey = RandomStringUtils.randomAlphabetic(64);
			settings.setSecretKey(secretKey);
			settings.save();
		}

		// create h2 or mysql connector
		DatabaseConnector connector = DatabaseConnectorFactory.createConnector(settings.getDatabase());

		if (connector == null) {

			log.error("Unknown database driver");
			System.exit(1);

		}

		// connect do database
		try {

			database.connect(connector);

			log.info("Establish connection successful");

		} catch (SQLException e) {

			log.error("Establish connection failed", e);
			System.exit(1);

		}

		// update database schema if needed
		log.info("Setup Database...");
		InputStream is = Main.class.getResourceAsStream("/updates.sql");

		DatabaseUpdater updater = new DatabaseUpdater(database, config.getVersion(), is, VERSION);
		updater.addUpdate("2.3.0", new BcryptHashUpdate());

		if (!updater.updateDB()) {
			System.exit(-1);
		}

		// create directories
		FileUtil.createDirectory(settings.getTempPath());
		FileUtil.createDirectory(settings.getLocalWorkspace());

		// insert fixtures
		Fixtures.insert(database);

		// start workflow engine
		try {

			WorkflowEngine engine = new PersistentWorkflowEngine(database, settings.getThreadsQueue(),
					settings.getThreadsSetupQueue());
			new Thread(engine).start();

			int port = Integer.parseInt(line.getOptionValue("port", settings.getPort()));

			// loggerFacade = new Slf4jLoggerFacade();
			// Engine.getInstance().setLoggerFacade(loggerFacade);

			log.info("Starting web server at port " + port);

			String webAppFolder = "";

			if (new File("webapp").exists()) {
				webAppFolder = "webapp";
			} else {
				webAppFolder = FileUtil.path("src", "main", "html", "webapp");
			}

			String pagesFolder = "";
			if (new File(config.getPages()).exists()) {
				pagesFolder = config.getPages();
			} else {
				pagesFolder = "sample/pages";
			}

			server = new WebServer();
			server.setPort(port);
			server.setRootDirectory(webAppFolder);
			server.setPagesDirectory(pagesFolder);
			if (settings.isHttps()) {
				String keystore = settings.getHttpsKeystore();
				String phrase = settings.getHttpsPassword();
				server.setHttpsCertificate(keystore, phrase);
			}
			server.setDatabase(database);
			server.setSettings(settings);
			server.setWorkflowEngine(engine);

			server.start();

		} catch (Exception e) {

			log.error("Can't launch the web server.\nAn unexpected " + "exception occured:", e);

			database.disconnect();

			System.exit(1);

		}

	}

	public static void main(String[] args) throws Exception {
		Main main = new Main();
		main.runCloudgene(null, args);
	}
}
