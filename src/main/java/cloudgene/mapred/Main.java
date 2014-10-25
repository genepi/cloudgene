package cloudgene.mapred;

import genepi.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.SQLException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.restlet.engine.Engine;
import org.restlet.ext.slf4j.Slf4jLoggerFacade;

import cloudgene.mapred.core.User;
import cloudgene.mapred.cron.CronJobScheduler;
import cloudgene.mapred.database.DatabaseUpdater;
import cloudgene.mapred.database.H2Connector;
import cloudgene.mapred.database.TemplateDao;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.util.Template;

public class Main {

	private static final Log log = LogFactory.getLog(Main.class);

	public static final String VERSION = "1.9.5";

	public static void main(String[] args) throws IOException {

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

		log.info("Cloudgene " + VERSION);

		URLClassLoader cl = (URLClassLoader) Main.class.getClassLoader();
		try {
			URL url = cl.findResource("META-INF/MANIFEST.MF");
			Manifest manifest = new Manifest(url.openStream());
			Attributes attr = manifest.getMainAttributes();
			String buildVesion = attr.getValue("Version");
			String buildTime = attr.getValue("Build-Time");
			String builtBy = attr.getValue("Built-By");
			log.info("Built by " + builtBy + " on " + buildTime);

		} catch (IOException E) {
			// handle
		}

		// create the command line parser
		CommandLineParser parser = new PosixParser();

		// create the Options
		Options options = new Options();
		Option portOption = new Option(null, "port", true,
				"runs cloudgene on port <PORT>");
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

		H2Connector connector = H2Connector.getInstance();
		try {

			connector.connect();

			log.info("Establish connection successful");

		} catch (SQLException e) {

			log.error("Establish connection failed", e);
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
				"version.txt", "/updates.sql", VERSION, false);
		if (askimedUpdater.needUpdate()) {
			log.info("Database needs update.");
			if (!askimedUpdater.update()) {
				log.error("Updating database failed.");
				return;
			}
		} else {
			log.info("Database is uptodate.");
		}

		String username = "admin";
		String password = "admin1978";

		// insert user
		UserDao dao = new UserDao();
		User user = dao.findByUsername(username);
		if (user == null) {
			user = new User();
			user.setUsername(username);
			password = HashUtil.getMD5(password);
			user.setPassword(password);
			user.setRole("admin");

			dao.insert(user);
			log.info("User " + username + " created.");
		} else {
			log.info("User " + username + " already exists.");
		}

		// inser messages
		TemplateDao htmlSnippetDao = new TemplateDao();

		for (Template defaultSnippet : Template.SNIPPETS) {

			Template snippet = htmlSnippetDao
					.findByKey(defaultSnippet.getKey());
			if (snippet == null) {
				htmlSnippetDao.insert(defaultSnippet);
				log.info("Template " + defaultSnippet.getKey() + " created.");
			} else {
				log.info("Template " + defaultSnippet.getKey()
						+ " already exists.");
			}

		}

		try {

			// load config

			Settings settings = Settings.getInstance();

			if (new File("config/settings.yaml").exists()) {

				settings.load("config/settings.yaml");
				// reload!
				settings = Settings.getInstance();

			} else {

				log.warn("Config file not found. (config/settings.yaml).");
				log.info("This is a fresh installation of Cloudgene. Init config with sample application.");

			}

			if (!settings.testPaths()) {

				H2Connector.getInstance().disconnect();

				System.exit(1);
			}

			// create directories
			FileUtil.createDirectory(settings.getTempPath());

			// load templates into memory
			settings.reloadTemplates();

			new Thread(WorkflowEngine.getInstance()).start();

			log.info("Start CronJobScheduler...");
			CronJobScheduler scheduler = new CronJobScheduler();
			scheduler.start(settings.isAutoRetire(),
					settings.isWriteStatistics());

			int port = Integer.parseInt(line.getOptionValue("port", "8082"));

			log.info("Starting web server at port " + port);

			String webAppFolder = "";

			if (new File("webapp").exists()) {
				webAppFolder = "webapp";
			} else {
				webAppFolder = FileUtil.path(new File(Main.class
						.getProtectionDomain().getCodeSource().getLocation()
						.getPath()).getParent(), "html", "webapp");
				System.out.println(webAppFolder);
			}

			String pagesFolder = "";
			if (new File("pages").exists()) {
				pagesFolder = "pages";
			} else {
				pagesFolder = "sample/pages";
			}

			new WebServer(webAppFolder, pagesFolder, port, settings.isHttps(),
					settings.getHttpsKeystore(), settings.getHttpsPassword())
					.start();

		} catch (Exception e) {

			log.error("Can't launch the web server.\nAn unexpected "
					+ "exception occured:", e);

			try {

				H2Connector.getInstance().disconnect();

			} catch (SQLException e1) {

				log.error("An unexpected " + "exception occured:", e);

			}

			System.exit(1);

		}
	}
}
