package cloudgene.mapred;

import cloudgene.mapred.database.util.DatabaseConnectorFactory;
import cloudgene.mapred.database.util.Fixtures;
import cloudgene.mapred.jobs.PersistentWorkflowEngine;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.util.BuildUtil;
import cloudgene.mapred.util.Config;
import cloudgene.mapred.util.Settings;
import com.esotericsoftware.yamlbeans.YamlReader;
import genepi.db.Database;
import genepi.db.DatabaseConnector;
import genepi.db.DatabaseUpdater;
import genepi.io.FileUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.restlet.engine.Engine;
import org.restlet.ext.slf4j.Slf4jLoggerFacade;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.sql.SQLException;

public class Main implements Daemon {

	public static final String VERSION = "1.30.4";

	private Database database;

	private WebServer server;

	public void runCloudgene(Settings settings, String[] args) throws Exception {

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
				System.out.println("Loading settings from " + settingsFilename + "...");
				settings = Settings.load(config);
			} else {
				settings = new Settings(config);
			}

			if (!settings.testPaths()) {
				System.exit(1);
			}
		}
		
		settings.checkTechnologies();

		// configure logger
		if (new File("config/log4j.properties").exists()) {

			PropertyConfigurator.configure("config/log4j.properties");

			Slf4jLoggerFacade loggerFacade = new Slf4jLoggerFacade();
			Engine.getInstance().setLoggerFacade(loggerFacade);

		} else {

			if (new File("log4j.properties").exists()) {
				PropertyConfigurator.configure("log4j.properties");

				Slf4jLoggerFacade loggerFacade = new Slf4jLoggerFacade();
				Engine.getInstance().setLoggerFacade(loggerFacade);

			}

		}

		Log log = LogFactory.getLog(Main.class);

		log.info("Cloudgene " + VERSION);
		log.info(BuildUtil.getBuildInfos());

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

		// init schema
		if (connector.isNewDatabase()) {

			InputStream is = Main.class.getResourceAsStream("/create-tables.sql");
			connector.executeSQL(is);

			File versionFile = new File(config.getVersion());
			if (versionFile.exists()) {
				versionFile.delete();
			}
		}

		// update database schema if needed
		InputStream is = Main.class.getResourceAsStream("/updates.sql");
		DatabaseUpdater askimedUpdater = new DatabaseUpdater(connector, config.getVersion(), is, VERSION);

		if (askimedUpdater.needUpdate()) {
			log.info("Database needs update.");
			if (!askimedUpdater.update()) {
				log.error("Updating database failed.");
				database.disconnect();
				System.exit(1);
			}
		} else {
			log.info("Database is up to date.");
		}

		// create directories
		FileUtil.createDirectory(settings.getTempPath());
		FileUtil.createDirectory(settings.getLocalWorkspace());

		// insert fixtures
		Fixtures.insert(database);

		// start workflow engine
		try {

			log.info("Start workflow engine.");

			WorkflowEngine engine = new PersistentWorkflowEngine(database, settings.getThreadsQueue(),
					settings.getThreadsSetupQueue());
			new Thread(engine).start();

			int port = Integer.parseInt(line.getOptionValue("port", config.getPort()));

			log.info("Setting web server at port " + port);

			Slf4jLoggerFacade loggerFacade = new Slf4jLoggerFacade();
			Engine.getInstance().setLoggerFacade(loggerFacade);

			String webAppFolder = "";

			if (new File("webapp").exists()) {
				webAppFolder = "webapp";
			} else {
				webAppFolder = FileUtil.path(
						new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent(),
						"html", "webapp");
			}
			log.info("Setting webapp folder: " + webAppFolder);

			String pagesFolder = "";
			if (new File(config.getPages()).exists()) {
				pagesFolder = config.getPages();
			} else {
				pagesFolder = "sample/pages";
			}
			log.info("Setting pages folder: " + pagesFolder);

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

			log.error("Can't launch the web server.\nAn unexpected " + "exception occurred:", e);

			database.disconnect();

			System.exit(1);

		}

	}

	@Override
	public void init(DaemonContext context) throws DaemonInitException, Exception {
		String[] args = context.getArguments();
		runCloudgene(null, args);
	}

	@Override
	public void start() throws Exception {

	}

	@Override
	public void destroy() {

	}

	@Override
	public void stop() throws Exception {
		server.stop();
		database.disconnect();
	}

	public static void main(String[] args) throws Exception {
		Main main = new Main();
		main.runCloudgene(null, args);
	}
}